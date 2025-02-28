package fr.unice.polytech.stepDefs.groupOrder;

import fr.unice.polytech.Facade;
import fr.unice.polytech.application.dto.AddressDTO;
import fr.unice.polytech.application.dto.DeliveryLocationDTO;
import fr.unice.polytech.application.exceptions.RestaurantNotFoundException;
import fr.unice.polytech.application.port.ILocationRepository;
import fr.unice.polytech.application.usecase.interfaces.ILocationService;
import fr.unice.polytech.domain.exceptions.MenuItemNotFoundException;
import fr.unice.polytech.domain.models.order.OrderStatus;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.groupOrder.GroupOrder;
import fr.unice.polytech.domain.models.groupOrder.GroupOrderStatus;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import fr.unice.polytech.infrastructure.repository.inmemory.LocationRepository;
import io.cucumber.java.en.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class JoinGroupOrderStepDefs {

    private String groupID;
    private Restaurant restaurant;
    private Order singleOrder;
    private GroupOrder groupOrder;
    private final String userID1;
    private final String userID2;
    private final ILocationService locationService;
    private final Facade facade;
    private Exception exception;
    private ILocationRepository locationRepository = new LocationRepository();

    public JoinGroupOrderStepDefs() throws RestaurantNotFoundException {
        this.facade = new Facade();
        this.locationService = this.facade.getLocationService();
        this.locationService.setLocationRepository(locationRepository);
        this.userID1 = facade.createUser("John Doe", "johndoe@gmail.com", "password").getId();
        this.userID2 = facade.createUser("Jane Doe", "janedoe@gmail.com", "password").getId();
        this.setDeliveryLocations();
        this.setRestaurant();
    }

    public void setDeliveryLocations() {
        List<Map<String, String>> deliveryLocations = List.of(
                Map.of("locationName", "Templiers", "street", "930 Route des Colles", "zipCode", "06410", "city", "Biot", "country", "France")
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
        assertEquals(order.getStatus(), OrderStatus.PENDING);
        this.facade.placeOrder(order.getId());
        assertEquals(order.getStatus(), OrderStatus.COMPLETED);
        return order;
    }

    @Given("a group order with the status {string}")
    public void aGroupOrderStatusGroupOrderWithTheStatus(String groupOrderStatus) throws MenuItemNotFoundException {
        LocalDateTime deliveryTime = LocalDateTime.now().plusMinutes(125);
        String deliveryLocationID = this.locationService.getLocationByName("Templiers A").orElseThrow(
                () -> new IllegalArgumentException("Location not found")
        ).getId();
        GroupOrderStatus status = GroupOrderStatus.valueOf(groupOrderStatus.toUpperCase());
        this.groupID = this.facade.createGroupOrder(this.userID2, deliveryLocationID, deliveryTime);
        this.groupOrder = this.facade.getGroupOrderById(this.groupID);
        assertEquals(GroupOrderStatus.INITIALIZED, this.groupOrder.getStatus());

        if (status == GroupOrderStatus.COMPLETING) {
            placeOrder(this.userID2, this.restaurant.getMenu().getFirst());
            this.facade.joinGroupOrder(this.userID1, this.groupID);
            this.singleOrder = this.facade.getOrderById(this.groupOrder.getSubOrderIDs().getLast());
        } else if (status == GroupOrderStatus.FINALISING) {
            placeOrder(this.userID2, this.restaurant.getMenu().getFirst());
            this.facade.joinGroupOrder(this.userID1, this.groupID);
            this.singleOrder = placeOrder(this.userID1, this.restaurant.getMenu().getFirst());
            this.facade.validateGroupOrder(this.singleOrder.getId(), null);
        }

        assertEquals(this.groupOrder.getStatus(), GroupOrderStatus.valueOf(groupOrderStatus.toUpperCase()));
    }

    @Given("a group order identifier {string}")
    public void aGroupOrderIdentifierGroupID(String groupID) {
        this.groupID = groupID;
    }

    @And("a pending sub-order associated with the group order")
    public void aPendingSubOrderAssociatedWithTheGroupOrder() {
        this.facade.joinGroupOrder(this.userID1, this.groupID);
        this.singleOrder = this.facade.getOrderById(this.groupOrder.getSubOrderIDs().getLast());
        assertTrue(this.groupOrder.getSubOrderIDs().contains(this.singleOrder.getId()));
        assertEquals(OrderStatus.PENDING, this.singleOrder.getStatus());
    }

    @When("the campus user joins the group order")
    public void theCampusUserJoinsTheGroupOrder() {
        try {
            this.facade.joinGroupOrder(this.userID1, this.groupID);
            this.singleOrder = this.facade.getOrderById(this.groupOrder.getSubOrderIDs().getLast());
        } catch (Exception exception) {
            this.exception = exception;
        }
    }

    @When("the campus user places the sub-order")
    public void theCampusUserPlacesTheSubOrder() throws MenuItemNotFoundException {
        this.singleOrder = placeOrder(this.userID1, this.restaurant.getMenu().getFirst());
    }

    @Then("the groupOrder is associated with a pending sub-order")
    public void theGroupOrderIsAssociatedWithAPendingSubOrder() {
        assertTrue(this.groupOrder.getSubOrderIDs().contains(this.singleOrder.getId()));
        assertEquals(this.singleOrder.getStatus(), OrderStatus.PENDING);
    }

    @Then("the sub-order is associated with the group order")
    public void theOrderIsAssociatedWithTheGroupOrder() {
        assertTrue(this.groupOrder.getSubOrderIDs().contains(this.singleOrder.getId()));
    }

    @And("the sub-order delivery details are the same as the group order's")
    public void theSubOrderDeliveryDetailsAreTheSameAsTheGroupOrderS() {
        assertTrue(this.locationService.getLocationById(this.groupOrder.getDeliveryLocationID()).isPresent());
        assertEquals((Object) this.locationService.getLocationById(this.groupOrder.getDeliveryLocationID()).get(), this.singleOrder.getDeliveryLocation());
    }

    @And("the group order status is {string}")
    public void theGroupOrderStatusIs(String groupOrderStatus) {
        assertEquals(GroupOrderStatus.valueOf(groupOrderStatus.toUpperCase()), this.groupOrder.getStatus());
    }

    @Then("the order joining error message {string} is displayed")
    public void theOrderJoiningErrorMessageIsDisplayed(String errorMessage) {
        assertEquals(errorMessage, this.exception.getMessage());
    }

}