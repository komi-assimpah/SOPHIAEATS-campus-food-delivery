package fr.unice.polytech.junit.domain.models.restaurant;

import fr.unice.polytech.domain.models.Address;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import fr.unice.polytech.domain.models.restaurant.discountstrategy.DiscountStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantTest {

    private Restaurant restaurant;
    private String id;
    private String name;
    private Address address;
    private MenuItem item1;
    private MenuItem item2;
    private List<MenuItem> menu;
    private Schedule schedule1;
    List<DiscountStrategy> discountStrategies;

    @BeforeEach
    void setUp() {
        id = "123";
        name = "Gourmet Place";
        address = new Address("930 Route des Colles", "06410", "Biot", "France");
        item1 = new MenuItem("ChocoBon", 10.0, 15);
        item2 = new MenuItem("Pizza", 8.0, 20);
        menu = Arrays.asList(item1, item2);
        schedule1 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0), 3);

        discountStrategies = new ArrayList<>();

        restaurant = new Restaurant(id, name, address);
    }

    @Test
    void testRestaurantSuccessfulCreation() {
        assertEquals(id, restaurant.getId());
        assertEquals(name, restaurant.getName());
        assertEquals(address, restaurant.getAddress());
    }

    @Test
    void testRestaurantInvalidCreation() {
        try {
            restaurant = new Restaurant(null, name, address);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Id cannot be null");
        }
        try {
            restaurant = new Restaurant(id, null, address);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Name cannot be null");
        }
        try {
            restaurant = new Restaurant(id, name, null);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Address cannot be null");
        }
    }

    @Test
    void testSetName() {
        restaurant.setName("Fast Food Régalé");
        assertEquals("Fast Food Régalé", restaurant.getName());
    }

    void testInvalidSetName() {

    }

    @Test
    void testSetSchedule() {
        restaurant.addSchedule(schedule1);
        restaurant.getSchedules().contains(schedule1);
        restaurant.addSchedule(new Schedule(DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(12, 0), 3));
        assertEquals(2, restaurant.getSchedules().size());
    }

    @Test
    void testSetMenu() {
        restaurant.addMenuItem(item1);
        restaurant.addMenuItem(item2);
        assertEquals(menu, restaurant.getMenu());
        assertEquals(2, restaurant.getMenu().size());
    }

    @Test
    void testSetAddress() {
        restaurant.setAddress(new Address("930 Route des Colles,06410,Biot,France"));
        assertEquals(new Address("930 Route des Colles", "06410", "Biot", "France"), restaurant.getAddress());
    }

    @Test
    void testGetters() {
        assertEquals(id, restaurant.getId());
        assertEquals(name, restaurant.getName());
        assertEquals(address, restaurant.getAddress());
    }


}
