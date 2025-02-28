package fr.unice.polytech.stepDefs.order;

import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import fr.unice.polytech.application.exceptions.RestaurantNotFoundException;
import fr.unice.polytech.domain.models.delivery.DeliveryLocation;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import fr.unice.polytech.domain.models.user.User;
import io.cucumber.java.en.*;
import fr.unice.polytech.Facade;
import fr.unice.polytech.domain.exceptions.MenuItemNotFoundException;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;

public class AdjustDeliveryTimeSteps {

    private Order currentOrder;
    private Restaurant selectedRestaurant;
    private final Facade facade;

    public AdjustDeliveryTimeSteps() {
        facade = new Facade();
    }

    @Given("the restaurant {string} has the following menu items:")
    public void theRestaurantHasTheFollowingMenuItems(String restaurantName, List<Map<String, String>> menuItems) {
        facade.createRestaurant(restaurantName, "Templiers A", "Nice", "06300", "France");
        String userID = facade.createUser("Jackie Chan", "jackiechan@gmail.com", "password").getId();
        selectedRestaurant = facade.findByName(restaurantName)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        for (Map<String, String> data : menuItems) {
            String itemName = data.get("name");
            double price = Double.parseDouble(data.get("price"));
            int prepTime = Integer.parseInt(data.get("preparation_time_in_minutes"));

            MenuItem menuItem = new MenuItem(itemName, price, prepTime);
            selectedRestaurant.addMenuItem(menuItem);
        }
    }

    @Given("the restaurant {string} is open on {string} from {string} to {string}")
    public void theRestaurantIsOpenOnFromTo(String restaurantName, String day, String openingTime, String closingTime){
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(day.toUpperCase());
        LocalTime openTime = LocalTime.parse(openingTime);
        LocalTime closeTime = LocalTime.parse(closingTime);

        selectedRestaurant = facade.findByName(restaurantName)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        Schedule schedule = new Schedule(dayOfWeek, openTime, closeTime, 5);

        try {
            facade.addSchedule(selectedRestaurant.getId(), schedule);
        } catch (RestaurantNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @And("the customer places an order whose delivery time was set to next Monday at {string}")
    public void aCustomerPlacesAnOrderWhoseDeliveryTimeWasSetToNextMondayAt(String deliveryTimeStr) {
        DeliveryLocation deliveryLocation = facade.selectDeliveryLocation("Templiers A")
                .orElseThrow(() -> new IllegalArgumentException("Delivery location not found"));
        User user = facade.createUser("Jackie Chan", "chan@gmail.com", "password");

        LocalDateTime deliveryTime;
        if (!LocalDate.now().with(DayOfWeek.MONDAY).isAfter(LocalDate.now())) {
            deliveryTime = LocalDateTime.now().with(DayOfWeek.MONDAY).with(LocalTime.parse(deliveryTimeStr)).plusDays(7);
        } else {
            deliveryTime = LocalDateTime.now().with(DayOfWeek.MONDAY).with(LocalTime.parse(deliveryTimeStr));
        }

        currentOrder = facade.createOrder(selectedRestaurant.getId(), user.getId(), deliveryLocation.getId(), deliveryTime);
        facade.placeOrder(currentOrder.getId());
    }

    @When("the customer adds a {string} to the order")
    public void theCustomerAddsAToTheOrder(String menuItemName) {
        try{
            try {
                MenuItem menuItem = selectedRestaurant.getMenuItemByName(menuItemName)
                        .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));
                System.out.println("Menu item: " + menuItem.getName());
                facade.addItems(currentOrder.getId(), menuItem.getId(), 1);
            } catch (MenuItemNotFoundException e) {
                fail("Menu item not found: " + menuItemName);
            }
        } catch (IllegalStateException e) {
            Logger.getGlobal().warning(e.getMessage());
        }

    }

    @Then("the system should show the earliest delivery time as next Monday at {string}")
    public void theSystemShouldShowTheEarliestDeliveryTimeAs(String expectedDeliveryTimeStr) {
        LocalDateTime expectedDeliveryTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.parse(expectedDeliveryTimeStr));
//        assertEquals(expectedDeliveryTime, currentOrder.getEarliestDeliveryTime());
    }

    @When("the customer browses for restaurants")
    public void theCustomerBrowsesForRestaurants() {
        // Implement logic to browse for restaurants
    }

    @Then("the restaurant should not appear in the list of available restaurants")
    public void theRestaurantShouldNotAppearInTheListOfAvailableRestaurants() {
        LocalDateTime orderTime = LocalDateTime.parse("2024-10-29T12:00:00");
        List<Restaurant> availableRestaurants = facade.getAvailableRestaurants(orderTime);
        assertFalse(availableRestaurants.contains(selectedRestaurant));
    }


    @And("the restaurant {string} is open on Monday from {string} to {string}")
    public void theRestaurantIsOpenOnMondayFromTo(String restaurantName, String startTime, String endTime) {
        Restaurant restaurant = facade.findByName(restaurantName)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        Schedule schedule = new Schedule(DayOfWeek.MONDAY, LocalTime.parse(startTime), LocalTime.parse(endTime), 5);
        try {
            facade.addSchedule(restaurant.getId(), schedule);
        } catch (RestaurantNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Then("the system should show the earliest delivery time as Monday at {string}")
    public void theSystemShouldShowTheEarliestDeliveryTimeAsMondayAt(String arg0) {
    }
}