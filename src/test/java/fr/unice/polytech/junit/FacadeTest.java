package fr.unice.polytech.junit;

import fr.unice.polytech.Facade;
import fr.unice.polytech.application.dto.AddressDTO;
import fr.unice.polytech.application.dto.DeliveryLocationDTO;
import fr.unice.polytech.application.exceptions.EntityNotFoundException;
import fr.unice.polytech.application.exceptions.RestaurantNotFoundException;
import fr.unice.polytech.application.usecase.interfaces.ILocationService;
import fr.unice.polytech.domain.exceptions.MenuItemNotFoundException;
import fr.unice.polytech.domain.models.delivery.DeliveryLocation;
import fr.unice.polytech.domain.models.order.OrderItem;
import fr.unice.polytech.domain.models.payment.PaymentDetails;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.groupOrder.GroupOrderStatus;
import fr.unice.polytech.domain.models.order.OrderStatus;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import fr.unice.polytech.domain.models.restaurant.discountstrategy.DiscountStrategy;
import fr.unice.polytech.domain.models.restaurant.discountstrategy.PercentageDiscount;
import fr.unice.polytech.domain.models.groupOrder.GroupOrder;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.Assert.*;

class FacadeTest {

    private Restaurant restaurant;
    private final String userID1;
    private final String userID2;
    private final String userID3;
    private final ILocationService locationService;
    private final Facade facade;

    FacadeTest() {
        this.facade = new Facade();
        this.locationService = this.facade.getLocationService();
        User user = facade.createUser("John Doe", "johndoe@gmail.com", "password");
        user.addPaymentMethod(new PaymentDetails("1234567890123456", "123", "12/23"));
        this.userID1 = user.getId();
        user = facade.createUser("Jane Doe", "janedoe@gmail.com", "password");
        user.addPaymentMethod(new PaymentDetails("1234567890123456", "123", "12/23"));
        this.userID2 = user.getId();
        this.userID3 = facade.createUser("Jack Doe", "jackdoe@gmail.com", "password").getId();
    }

    @BeforeEach
    void setUp() throws RestaurantNotFoundException {
        this.setDeliveryLocations();
        this.setRestaurant();
    }

    public void setDeliveryLocations() {
        List<Map<String, String>> deliveryLocations = List.of(
                Map.of("locationName", "Templiers A", "street", "930 Route des Colles", "zipCode", "06410", "city", "Biot", "country", "France"),
                Map.of("locationName", "Templiers B", "street", "930 Route des Colles", "zipCode", "06410", "city", "Biot", "country", "France")
        );
        for (Map<String, String> location : deliveryLocations) {
            try {
                String locationName = location.get("locationName");
                String street = location.get("street");
                String zipCode = location.get("zipCode");
                String city = location.get("city");
                String country = location.get("country");

                AddressDTO addressDTO = new AddressDTO(street, zipCode, city, country);
                DeliveryLocationDTO deliveryLocationDTO = new DeliveryLocationDTO(locationName, addressDTO);
                this.locationService.addLocation(deliveryLocationDTO);
            } catch (Exception e) {
                Logger.getGlobal().warning(e.getMessage());
            }
        }
    }

    private void setRestaurant() throws RestaurantNotFoundException {
        List<MenuItem> menuItems = List.of(
                new MenuItem("Fries", 2.0, 6),
                new MenuItem("Big Mac", 5.0, 9)
        );
        List<Schedule> schedulesTable = List.of(
                new Schedule(DayOfWeek.MONDAY, LocalTime.of(0, 0), LocalTime.of(23, 59), 5),
                new Schedule(DayOfWeek.TUESDAY, LocalTime.of(0, 0), LocalTime.of(23, 59), 5),
                new Schedule(DayOfWeek.WEDNESDAY, LocalTime.of(0, 0), LocalTime.of(23, 59), 5),
                new Schedule(DayOfWeek.THURSDAY, LocalTime.of(0, 0), LocalTime.of(23, 59), 5),
                new Schedule(DayOfWeek.FRIDAY, LocalTime.of(0, 0), LocalTime.of(23, 59), 5),
                new Schedule(DayOfWeek.SATURDAY, LocalTime.of(0, 0), LocalTime.of(23, 59), 5),
                new Schedule(DayOfWeek.SUNDAY, LocalTime.of(0, 0), LocalTime.of(23, 59), 5)
        );

        this.facade.createRestaurant("McDonald's", "400 Route des Colles", "Biot", "06410", "France");
        this.restaurant = this.facade.findByName("McDonald's")
                .orElseThrow(() -> new IllegalStateException("Restaurant not found"));
        this.restaurant.setOpeningHours(schedulesTable);
        this.restaurant.setMenu(menuItems);
    }

    private Order placeOrder(String userID, MenuItem menuItem) throws MenuItemNotFoundException {
        Order order = this.facade.chooseRestaurant(userID, this.restaurant.getId());
        this.facade.addItems(order.getId(), menuItem.getId(), 5);
        assertEquals(OrderStatus.PENDING, order.getStatus());
        this.facade.placeOrder(order.getId());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        return order;
    }

    @Test
    void testCreateGroupOrder() {
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class,
                () -> facade.createGroupOrder(userID1, null, null));
        assertEquals("Location not found", exception1.getMessage());

        String deliveryLocationID = locationService.getLocationByName("Templiers A").orElseThrow().getId();
        assertNotNull(facade.createGroupOrder(userID1, deliveryLocationID, null));

        LocalDateTime deliveryTime1 =  LocalDateTime.now().plusDays(1).with(LocalTime.parse("12:00"));
        assertNotNull(facade.createGroupOrder(userID2, deliveryLocationID, deliveryTime1));

        LocalDateTime deliveryTime2 = LocalDateTime.now().plusMinutes(-15);
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class,
                () -> facade.createGroupOrder(userID3, deliveryLocationID, deliveryTime2));
        assertEquals("Invalid delivery time!!", exception2.getMessage());
    }

    @Test
    void testJoinGroupOrder() throws MenuItemNotFoundException {
        EntityNotFoundException exception1 = assertThrows(EntityNotFoundException.class,
                () -> facade.joinGroupOrder(userID2, "00000000"));
        assertEquals("Group order with id 00000000 not found", exception1.getMessage());

        String deliveryLocationID = locationService.getLocationByName("Templiers A").orElseThrow().getId();

        GroupOrder groupOrder = facade.getGroupOrderById(facade.createGroupOrder(userID1, deliveryLocationID, null));
        assertFalse(groupOrder.getSubOrderIDs().isEmpty());

        Order order1 = facade.getOrderCoordinator().getUserCart(userID1).orElseThrow();
        assertFalse(facade.getGroupOrderService().getAllGroupOrders().isEmpty());
        groupOrder.setStatus(GroupOrderStatus.IN_PREPARATION);
        assertTrue(facade.getGroupOrderService().getAllGroupOrders().contains(groupOrder));

        DeliveryLocation deliveryLocation2 = locationService.getLocationByName("Templiers B").orElseThrow();
        Order temp0 = order1;
        IllegalStateException exception2 = assertThrows(IllegalStateException.class,
                () -> temp0.setDeliveryLocation(deliveryLocation2, true));
        assertEquals("Sub-order's group delivery address already set", exception2.getMessage());

        IllegalStateException exception3 = assertThrows(IllegalStateException.class,
                () -> facade.joinGroupOrder(userID2, groupOrder.getGroupID()));
        assertEquals("Group order already closed", exception3.getMessage());

        groupOrder.setStatus(GroupOrderStatus.INITIALIZED);
        facade.joinGroupOrder(userID2, groupOrder.getGroupID());
        Order order2 = facade.getOrderCoordinator().getUserCart(userID2).orElseThrow();
        assertEquals(GroupOrderStatus.INITIALIZED, groupOrder.getStatus());

        placeOrder(userID2, restaurant.getMenu().getFirst());
        assertTrue(groupOrder.getSubOrderIDs().contains(order2.getId()));
        assertEquals(groupOrder.getDeliveryLocationID(), ((DeliveryLocation) order2.getDeliveryLocation()).getId());
        assertEquals(GroupOrderStatus.COMPLETING, groupOrder.getStatus());

        order1 = placeOrder(userID1, restaurant.getMenu().getFirst());
        assertTrue(groupOrder.getSubOrderIDs().contains(order1.getId()));
        assertEquals(groupOrder.getDeliveryLocationID(), ((DeliveryLocation) order1.getDeliveryLocation()).getId());
        assertEquals(GroupOrderStatus.COMPLETING, groupOrder.getStatus());

        assertTrue(groupOrder.getSubOrderIDs().contains(order1.getId()));
        assertTrue(groupOrder.getSubOrderIDs().contains(order2.getId()));

        Order temp1 = order2;
        IllegalStateException exception4 = assertThrows(IllegalStateException.class,
                () -> temp1.setDeliveryTime(LocalDateTime.now().plusDays(1).with(LocalTime.parse("12:00")), false));
        assertEquals("Sub-order's delivery time to set in group", exception4.getMessage());

        groupOrder.setDeliveryTime(LocalDateTime.now().plusMinutes(125));

        Order temp2 = order1;
        IllegalStateException exception5 = assertThrows(IllegalStateException.class,
                () -> temp2.setDeliveryTime(LocalDateTime.now().plusDays(1).with(LocalTime.parse("12:00")), false));
        assertEquals("Sub-order's delivery time to set in group", exception5.getMessage());

        IllegalStateException exception6 = assertThrows(IllegalStateException.class,
                () -> groupOrder.setDeliveryTime(LocalDateTime.now().plusDays(1).with(LocalTime.parse("12:00"))));
        assertEquals("Group order delivery time already set", exception6.getMessage());

        assertEquals(groupOrder.getDeliveryLocationID(), ((DeliveryLocation) order1.getDeliveryLocation()).getId());
        assertEquals(groupOrder.getDeliveryLocationID(), ((DeliveryLocation) order2.getDeliveryLocation()).getId());
    }

    @Test
    void testValidateGroupOrder() throws MenuItemNotFoundException {
        String deliveryLocationID = locationService.getLocationByName("Templiers A").orElseThrow().getId();
        GroupOrder groupOrder = facade.getGroupOrderById(facade.createGroupOrder(userID1, deliveryLocationID, null));
        Order order1 = facade.getOrderCoordinator().getUserCart(userID1).orElseThrow();
        placeOrder(userID1, restaurant.getMenu().getFirst());
        groupOrder.setStatus(GroupOrderStatus.FINALISING);
        IllegalStateException exception1 = assertThrows(IllegalStateException.class,
                () -> facade.validateGroupOrder(order1.getId(), LocalDateTime.now().plusMinutes(130)));
        assertEquals("Group order already validated", exception1.getMessage());

        groupOrder.setStatus(GroupOrderStatus.INITIALIZED);
        IllegalStateException exception2 = assertThrows(IllegalStateException.class,
                () -> facade.validateGroupOrder(order1.getId(), LocalDateTime.now().plusMinutes(130)));
        assertEquals("Group order not in completing state", exception2.getMessage());

        groupOrder.setStatus(GroupOrderStatus.COMPLETING);
        IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class,
                () -> facade.validateGroupOrder(order1.getId(), null));
        assertEquals("Expected delivery time must be set", exception3.getMessage());
        assertEquals(GroupOrderStatus.COMPLETING, groupOrder.getStatus());

        LocalDateTime deliveryTime1 = LocalDateTime.now().plusMinutes(-15);
        IllegalArgumentException exception4 = assertThrows(IllegalArgumentException.class,
                () -> facade.validateGroupOrder(order1.getId(), deliveryTime1));
        assertEquals("Invalid delivery time", exception4.getMessage());
        assertEquals(GroupOrderStatus.COMPLETING, groupOrder.getStatus());


        LocalDateTime deliveryTime2 =  LocalDateTime.now().plusDays(1).with(LocalTime.parse("12:00"));
        facade.validateGroupOrder(order1.getId(), deliveryTime2);
        assertEquals(deliveryTime2, groupOrder.getDeliveryTime());
        assertEquals(GroupOrderStatus.FINALISING, groupOrder.getStatus());

        DeliveryLocation deliveryLocation2 = locationService.getLocationByName("Templiers B").orElseThrow();
        GroupOrder groupOrder2 = facade.getGroupOrderById(facade.createGroupOrder(userID2, deliveryLocation2.getId(), deliveryTime2));
        Order order2 = placeOrder(userID2, restaurant.getMenu().getFirst());

        IllegalStateException exception5 = assertThrows(IllegalStateException.class,
                () -> facade.validateGroupOrder(order2.getId(), deliveryTime2));
        assertEquals("Group order delivery time already set", exception5.getMessage());
        assertEquals(GroupOrderStatus.COMPLETING, groupOrder2.getStatus());

        facade.validateGroupOrder(order2.getId(), null);
        assertEquals(deliveryTime2, groupOrder2.getDeliveryTime());
        assertEquals(GroupOrderStatus.FINALISING, groupOrder2.getStatus());
    }

    @Test
    void testConfirmGroupOrder() throws MenuItemNotFoundException {
        LocalDateTime deliveryTime = LocalDateTime.now().plusDays(1).with(LocalTime.parse("12:00"));;
        String deliveryLocationID = locationService.getLocationByName("Templiers A").orElseThrow().getId();
        GroupOrder groupOrder = facade.getGroupOrderById(facade.createGroupOrder(userID1, deliveryLocationID, null));
        Order order1 = facade.getOrderCoordinator().getUserCart(userID1).orElseThrow();

        String orderID0 = order1.getId();
        IllegalStateException exception1 = assertThrows(IllegalStateException.class,
                () -> facade.placeOrder(orderID0));
        assertEquals("Order restaurant not chosen", exception1.getMessage());
        facade.chooseRestaurant(userID1, restaurant.getId());

        placeOrder(userID1, restaurant.getMenu().getFirst());
        IllegalStateException exception2 = assertThrows(IllegalStateException.class,
                () -> facade.confirmGroupOrder(orderID0));
        assertEquals("Sub-order's bill not paid", exception2.getMessage());

        facade.payOrder(order1.getId());
        groupOrder.setStatus(GroupOrderStatus.IN_PREPARATION);
        IllegalStateException exception3 = assertThrows(IllegalStateException.class,
                () -> facade.confirmGroupOrder(orderID0));
        assertEquals("Group order already confirmed", exception3.getMessage());

        Order order2 = facade.createOrder(restaurant.getId(), userID2, groupOrder.getDeliveryLocationID(), deliveryTime);
        String orderID1 = order2.getId();
        facade.placeOrder(orderID1);
        assertNull(facade.getGroupOrderService().findSubOrderGroup(orderID1));

        groupOrder.setStatus(GroupOrderStatus.COMPLETING);
        IllegalStateException exception4 = assertThrows(IllegalStateException.class,
                () -> facade.confirmGroupOrder(orderID0));
        assertEquals("Group order not in finalising state", exception4.getMessage());
        assertEquals(GroupOrderStatus.COMPLETING, groupOrder.getStatus());

        groupOrder = facade.getGroupOrderById(facade.createGroupOrder(userID1, deliveryLocationID, deliveryTime));
        order1 = facade.getOrderCoordinator().getUserCart(userID1).orElseThrow();

        String orderID2 = order1.getId();
        facade.chooseRestaurant(userID1, restaurant.getId());
        placeOrder(userID1, restaurant.getMenu().getFirst());
        assertEquals(groupOrder, facade.getGroupOrderService().findSubOrderGroup(orderID2));
        assertEquals(deliveryTime, groupOrder.getDeliveryTime());
        facade.validateGroupOrder(orderID2, null);
        order1.setStatus(OrderStatus.COMPLETED);
        IllegalStateException exception6 = assertThrows(IllegalStateException.class,
                () -> facade.confirmGroupOrder(orderID2));
        assertEquals("Sub-order's bill not paid", exception6.getMessage());

        assertEquals(0.0, facade.getUser(userID1).getBalance(), 0.01);

        DiscountStrategy discountStrategy = new PercentageDiscount(15, 10);
        facade.addDiscountStrategy(discountStrategy, restaurant.getId());
        facade.payOrder(order1.getId());
        facade.confirmGroupOrder(order1.getId());
        assertEquals(5, facade.getGroupOrderService().findSubOrderGroup(order1.getId()).getSubOrderIDs().stream().mapToInt(
                subOrderID -> facade.getOrderCoordinator().getOrderById(subOrderID).getOrderItems().stream().mapToInt(OrderItem::getQuantity).sum()
                    ).sum());
        assertEquals(GroupOrderStatus.IN_PREPARATION, groupOrder.getStatus());
        assertEquals(0.0, facade.getUser(userID1).getBalance(), 0.01);

        GroupOrder groupOrder2 = facade.getGroupOrderById(facade.createGroupOrder(userID2, deliveryLocationID, null));
        Order order4 = facade.getOrderCoordinator().getUserCart(userID2).orElseThrow();

        placeOrder(userID2, restaurant.getMenu().getFirst());
        facade.payOrder(order4.getId());

        facade.joinGroupOrder(userID3, groupOrder2.getGroupID());
        facade.getUser(userID3).addPaymentMethod(new PaymentDetails("123456789012", "12/23", "123"));
        Order order3 = facade.getOrderCoordinator().getUserCart(userID3).orElseThrow();
        placeOrder(userID3, restaurant.getMenu().getFirst());
        facade.payOrder(order3.getId());
        facade.validateGroupOrder(order3.getId(), deliveryTime);
        facade.confirmGroupOrder(order3.getId());

        double balance = order3.getTotalAmount() * 0.15;
        assertEquals(balance, facade.getUser(userID3).getBalance(), 0.01);
        assertEquals(balance, facade.getUser(userID2).getBalance(), 0.01);
    }

}
