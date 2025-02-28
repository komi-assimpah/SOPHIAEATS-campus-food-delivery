package fr.unice.polytech.junit.domain.models.restaurant;

import fr.unice.polytech.domain.models.restaurant.MenuItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

class MenuItemTest {

    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        menuItem = new MenuItem("Pizza", 12.99, 10);
    }

    @Test
    void testGetters() {
        assertEquals("Pizza", menuItem.getName());
        assertEquals(12.99, menuItem.getPrice(), 0.01);
        assertEquals(10, menuItem.getPreparationTimeInMinutes());
    }

    @Test
    void testSetters() {
        menuItem.setName("Burger");
        menuItem.setPrice(9.99);
        menuItem.setPreparationTime(15);

        assertEquals("Burger", menuItem.getName());
        assertEquals(9.99, menuItem.getPrice(), 0.01);
        assertEquals(15, menuItem.getPreparationTimeInMinutes());
    }


    @Test
    void testSetNegativePriceThrowsException() {
        menuItem.setPrice(-1.0);
        assertEquals(0.0, menuItem.getPrice(), 0.01);
    }

    @Test
    void testSetNegativePreparationTime() {
        menuItem.setPreparationTime(-5);
        assertEquals(1, menuItem.getPreparationTimeInMinutes());
    }

    @Test
    void testIsAvailableWhenDeliveryTimeAfterPreparation() {
        LocalTime orderTime = LocalTime.of(12, 0);
        LocalTime deliveryTime = LocalTime.of(12, 15);
        assertTrue(menuItem.isAvailable(orderTime, deliveryTime));
    }

    @Test
    void testIsAvailableWhenDeliveryTimeBeforePreparation() {
        LocalTime orderTime = LocalTime.of(12, 0);
        LocalTime deliveryTime = LocalTime.of(12, 5);
        assertFalse(menuItem.isAvailable(orderTime, deliveryTime));
    }

    @Test
    void testIsAvailableWithinPreparationTime() {
        LocalTime orderTime = LocalTime.of(12, 0);
        LocalTime deliveryTime = LocalTime.of(12, 15);
        assertTrue(menuItem.isAvailable(orderTime, deliveryTime));

        deliveryTime = LocalTime.of(12, 5);
        assertFalse(menuItem.isAvailable(orderTime, deliveryTime));
    }

    @Test
    void testIsAvailableWithInsufficientTime() {
        LocalTime orderTime = LocalTime.of(12, 0);
        LocalTime deliveryTime = LocalTime.of(12, 5);
        assertFalse(menuItem.isAvailable(orderTime, deliveryTime));
    }

    @Test
    void testEqualsAndHashCode() {
        MenuItem anotherMenuItem = new MenuItem(menuItem.getId(), "Pizza", 12.99, 10);
        assertEquals(menuItem, anotherMenuItem);
        assertEquals(menuItem.hashCode(), anotherMenuItem.hashCode());

        anotherMenuItem.setName("Burger");
        assertNotEquals(menuItem, anotherMenuItem);
    }

    @Test
    void testEqualsSameNameButDifferentPrice() {
        MenuItem differentPriceItem = new MenuItem("Pizza", 13.99, 10);
        //assertNotEquals(menuItem.getPrice(), differentPriceItem.getPrice());
        assertEquals(menuItem, differentPriceItem);
    }

    @Test
    void testEqualsSameNameButDifferentPreparationTime() {
        MenuItem differentPreparationTimeItem = new MenuItem("Pizza", 12.99, 15);
        //assertNotEquals(menuItem.getPreparationTimeInMinutes(), differentPreparationTimeItem.getPreparationTimeInMinutes());
        assertEquals(menuItem, differentPreparationTimeItem);
    }

    @Test
    void testNotEqualsDifferentName() {
        MenuItem differentNameItem = new MenuItem("Burger", 12.99, 10);
        assertNotEquals(menuItem, differentNameItem);
    }




}
