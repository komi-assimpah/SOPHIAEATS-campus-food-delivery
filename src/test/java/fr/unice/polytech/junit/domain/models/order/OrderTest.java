package fr.unice.polytech.junit.domain.models.order;

import fr.unice.polytech.domain.models.Address;
import fr.unice.polytech.domain.models.delivery.DeliveryLocation;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.order.OrderItem;
import fr.unice.polytech.domain.models.order.OrderStatus;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.user.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {

    private final User user = new User("1", "John", "Doe", "password");
    private final Restaurant restaurant = new Restaurant("1", "McDonalds", new Address("some street", "some city", "some zip code", "some country"));
    private final DeliveryLocation deliveryLocation = new DeliveryLocation("1", new Address("some street", "some city", "some zip code", "some country"));


    //ORDER BUILDER TESTS
    @Test
    void testOrderBuilder() {
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), null).build();

        assertEquals("1", order.getId());
        assertEquals(order.getUser(), user);
        assertEquals(order.getRestaurant(), restaurant);
        assertEquals(order.getDeliveryLocation(), deliveryLocation);
        assertNotNull(order.getDeliveryTime());
        assertEquals(0, order.getTotalAmount());
    }

    @Test
    void testOrderBuilderWithOrderTime() {
        LocalDateTime orderTime = LocalDateTime.now();
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), orderTime).build();

        assertEquals("1", order.getId());
        assertEquals(order.getUser(), user);
        assertEquals(order.getRestaurant(), restaurant);
        assertEquals(order.getDeliveryLocation(), deliveryLocation);
        assertNotNull(order.getDeliveryTime());
        assertEquals(0, order.getTotalAmount());
        assertEquals(order.getOrderTime(), orderTime);
        assertEquals(order.getStatus(), OrderStatus.PENDING);
    }

    @Test
    void testOrderBuilderWithStatus() {
        LocalDateTime orderTime = LocalDateTime.now();
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), orderTime)
                .setStatus(OrderStatus.PENDING)
                .build();

        assertEquals("1", order.getId());
        assertEquals(order.getUser(), user);
        assertEquals(order.getRestaurant(), restaurant);
        assertEquals(order.getDeliveryLocation(), deliveryLocation);
        assertNotNull(order.getDeliveryTime());
        assertEquals(0, order.getTotalAmount());
        assertEquals(order.getOrderTime(), orderTime);
        assertEquals(order.getStatus(), OrderStatus.PENDING);
    }

    // Tests de OrderBuilder avec les champs requis
    @Test
    void testOrderBuilderWithRequiredFields() {
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), LocalDateTime.now()).build();
        assertNotNull(order);
        assertEquals("1", order.getId());
        assertEquals(user, order.getUser());
        assertEquals(restaurant, order.getRestaurant());
        assertEquals(deliveryLocation, order.getDeliveryLocation());
    }


    @Test
    void testOrderBuilderMissingRequiredFields() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new Order.OrderBuilder("1", null, restaurant, deliveryLocation, LocalDateTime.now(), LocalDateTime.now()).build()
        );
        assertEquals("Order is missing required fields", exception.getMessage());
    }

    @Test
    void testSetOrderUser() {
        Order order = new Order.OrderBuilder("1", null, restaurant, deliveryLocation, LocalDateTime.now(), LocalDateTime.now())
                .setOrderUser(user)
                .build();

        assertEquals(user, order.getUser());
    }

    @Test
    void testSetOrderDeliveryTime() {
        LocalDateTime deliveryTime = LocalDateTime.now().plusDays(1);
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), LocalDateTime.now())
                .setOrderDeliveryTime(deliveryTime)
                .build();

        assertEquals(deliveryTime, order.getDeliveryTime());
    }

    @Test
    void testSetOrderDeliveryLocation() {
        DeliveryLocation newDeliveryLocation = new DeliveryLocation("2", new Address("other street", "other city", "other zip code", "other country"));
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), LocalDateTime.now())
                .setOrderDeliveryLocation(newDeliveryLocation)
                .build();

        assertEquals(newDeliveryLocation, order.getDeliveryLocation());
    }

    @Test
    void testSetOrderStatus() {
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), LocalDateTime.now())
                .setStatus(OrderStatus.DELIVERED)
                .build();

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void testSetOrderItems() {
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(new MenuItem("Big Mac", 5.0, 10), 2));
        items.add(new OrderItem(new MenuItem("Fries", 2.0, 20), 1));

        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), LocalDateTime.now())
                .setOrderItems(items)
                .build();

        assertEquals(items, order.getOrderItems());
        assertEquals(2, order.getOrderItems().size());
    }

    @Test
    void testSetOderStatus() {
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), LocalDateTime.now())
                .setStatus(OrderStatus.CANCELLED)
                .build();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }


    @Test
    void testSetOrderRestaurant() {
        Restaurant newRestaurant = new Restaurant("2", "KFC", new Address("another street", "another city", "another zip code", "another country"));
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), LocalDateTime.now())
                .setOrderRestaurant(newRestaurant)
                .build();

        assertEquals(newRestaurant, order.getRestaurant());
    }


    //ORDER TESTS
    @Test
    void testAddOrderItem() {
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), null).build();

        assertTrue(order.getOrderItems().isEmpty());

        MenuItem menuItem = new MenuItem("1", "Big Mac", 5.0, 10);

        order.addOrderItem(menuItem, 2);
        assertEquals(1, order.getOrderItems().size());
        assertEquals(order.getOrderItems().get(0).getItem(), menuItem);
        assertEquals(2, order.getOrderItems().get(0).getQuantity());
    }

    @Test
    void testAddOrderItemWithExistingItem() {
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), null).build();

        assertTrue(order.getOrderItems().isEmpty());

        MenuItem menuItem = new MenuItem("1", "Big Mac", 5.0, 10);
        MenuItem menuItem2 = new MenuItem("2", "Fries", 2.0, 10);

        order.addOrderItem(menuItem, 2);
        order.addOrderItem(menuItem2, 3);
        assertEquals(2, order.getOrderItems().size());

        order.addOrderItem(menuItem, 3);
        assertEquals(2, order.getOrderItems().size());
        assertEquals(order.getOrderItems().get(0).getItem(), menuItem);
        assertEquals(5, order.getOrderItems().get(0).getQuantity());
    }

    @Test
    void testRemoveOrderItem() {
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), null).build();

        assertTrue(order.getOrderItems().isEmpty());

        MenuItem menuItem = new MenuItem("1", "Big Mac", 5.0, 10);

        order.addOrderItem(menuItem, 2);
        assertEquals(1, order.getOrderItems().size());

        order.removeOrderItem(menuItem);
        assertTrue(order.getOrderItems().isEmpty());
    }

    @Test
    void testRemoveOrderItemWithMultipleItems() {
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), null).build();

        assertTrue(order.getOrderItems().isEmpty());

        MenuItem menuItem = new MenuItem("1", "Big Mac", 5.0, 10);
        MenuItem menuItem2 = new MenuItem("2", "Fries", 2.0, 10);

        order.addOrderItem(menuItem, 2);
        order.addOrderItem(menuItem2, 3);
        assertEquals(2, order.getOrderItems().size());

        order.removeOrderItem(menuItem);
        assertEquals(1, order.getOrderItems().size());
        assertEquals(order.getOrderItems().get(0).getItem(), menuItem2);
    }

    @Test
    void testClearOrderItems() {
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), null).build();

        assertTrue(order.getOrderItems().isEmpty());

        MenuItem menuItem = new MenuItem("1", "Big Mac", 5.0, 10);
        MenuItem menuItem2 = new MenuItem("2", "Fries", 2.0, 10);

        order.addOrderItem(menuItem, 2);
        order.addOrderItem(menuItem2, 3);
        assertEquals(2, order.getOrderItems().size());

        assertEquals(order.getStatus(), OrderStatus.PENDING);
        order.clearOrderItems();
        assertTrue(order.getOrderItems().isEmpty());
    }

    @Test
    void testClearOrderItemsThrowsExceptionIfNotPending() {
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), null).build();
        assertTrue(order.getOrderItems().isEmpty());

        MenuItem menuItem = new MenuItem("1", "Big Mac", 5.0, 10);
        order.addOrderItem(menuItem, 2);
        assertFalse(order.getOrderItems().isEmpty());
        order.setStatus(OrderStatus.IN_PROGRESS);

        assertEquals("Cannot clear order items for an order that is not pending",
                assertThrows(IllegalStateException.class, order::clearOrderItems).getMessage());

        assertFalse(order.getOrderItems().isEmpty());


    }

    @Test
    void testCalculateTotalAmount() {
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), null).build();

        assertTrue(order.getOrderItems().isEmpty());

        MenuItem menuItem = new MenuItem("1", "Big Mac", 5.0, 10);
        MenuItem menuItem2 = new MenuItem("2", "Fries", 2.0, 10);

        order.addOrderItem(menuItem, 2);
        order.addOrderItem(menuItem2, 3);
        assertEquals(16, order.getTotalAmount());
    }

    @Test
    void testSetDeliveryTime() {
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), null).build();

        assertNotNull(order.getDeliveryTime());

        LocalDateTime deliveryTime = LocalDateTime.now().plusHours(1);
        order.setDeliveryTime(deliveryTime, false);
        assertEquals(deliveryTime, order.getDeliveryTime());
    }

    @Test
    void testSetStatus() {
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), null).build();

        assertEquals(OrderStatus.PENDING, order.getStatus());

        order.setStatus(OrderStatus.DELIVERED);
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void testSetRestaurant() {
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), null).build();

        assertEquals(restaurant, order.getRestaurant());

        Restaurant newRestaurant = new Restaurant("2", "KFC", new Address("some street", "some city", "some zip code", "some country"));
        order.setRestaurant(newRestaurant);
        assertEquals(newRestaurant, order.getRestaurant());
    }

    @Test
    void testSetDeliveryLocation() {
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), null).build();

        assertEquals(deliveryLocation, order.getDeliveryLocation());

        DeliveryLocation newDeliveryLocation = new DeliveryLocation("2", new Address("some street", "some city", "some zip code", "some country"));
        order.setDeliveryLocation(newDeliveryLocation, false);
        assertEquals(newDeliveryLocation, order.getDeliveryLocation());
    }

    @Test
    void testSetTotalAmount() {
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), null).build();

        assertEquals(0, order.getTotalAmount());

        order.setTotalAmount(10);
        assertEquals(10, order.getTotalAmount());
    }

    @Test
    void testIsGroupOrder() {
        Order order = new Order.OrderBuilder("1", user, restaurant, deliveryLocation, LocalDateTime.now(), null).build();

        assertFalse(order.isSubOrder());
    }

}