package fr.unice.polytech.stepDefs.order;

import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import fr.unice.polytech.domain.models.delivery.DeliveryLocation;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import fr.unice.polytech.domain.models.user.User;
import io.cucumber.java.en.*;
import fr.unice.polytech.Facade;
import fr.unice.polytech.domain.exceptions.MenuItemNotFoundException;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;

public class AddMenuSteps {

    private final Facade facade;
    private Order currentOrder;
    private Restaurant selectedRestaurant;
    private String currentOrderId;
    private String selectedRestaurantId;
    private String userId;
    private LocalDateTime deliveryTime;
    private DeliveryLocation deliveryLocation;
    private LocalDateTime orderDeliveryTime;

    public AddMenuSteps() {
        facade = new Facade();
    }

    @Given("the user {string} with email {string} is logged in with the password {string}")
    public void theUserIsLoggedIn(String name, String email, String password) {
        User user = facade.createUser(name, email, password);
        userId = user.getId();
    }

    @Given("the following restaurants exist:")
    public void theFollowingRestaurantsExist(List<Map<String, String>> restaurants) {
        for (Map<String, String> data : restaurants) {
            try {
                DayOfWeek dayOfWeek = DayOfWeek.valueOf(data.get("day").toUpperCase());
                LocalTime openingTime = LocalTime.parse(data.get("opening_hours"));
                LocalTime closingTime = LocalTime.parse(data.get("closing_hours"));
                int staffCount = Integer.parseInt(data.get("staff_count"));
                Schedule schedule = new Schedule(dayOfWeek, openingTime, closingTime, staffCount);

                Restaurant resto =  facade.createRestaurant(
                        data.get("name"), data.get("street"), data.get("city"), data.get("zip_code"), data.get("country")
                );
                facade.addSchedule(resto.getId(), schedule);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Given("the following menu items exist in the restaurant {string}:")
    public void theFollowingMenuItemsExistInTheRestaurant(String restaurantName, List<Map<String, String>> menuItems) {
        selectedRestaurant = facade.findByName(restaurantName)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        for (Map<String, String> data : menuItems) {
            String itemName = data.get("name");
            double price = Double.parseDouble(data.get("price"));
            int prepTime = Integer.parseInt(data.get("preparation_time"));

            MenuItem menuItem = new MenuItem(itemName, price, prepTime);
            selectedRestaurant.addMenuItem(menuItem);
        }
    }

    @Given("the user selects the restaurant {string}")
    public void theUserSelectsTheRestaurant(String restaurantName) {
        Restaurant restaurant = facade.findByName(restaurantName)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        selectedRestaurant = facade.browseRestaurant(restaurant.getId());
        assertNotNull(selectedRestaurant, "Restaurant should be selected successfully.");
        selectedRestaurantId = restaurant.getId();
    }

    @Given("no restaurant is selected")
    public void noRestaurantIsSelected() {
        selectedRestaurant = null;
        selectedRestaurantId = null;
    }

    @When("the user tries to add {string} with a quantity of {int}")
    public void theUserTriesToAddWithAQuantityOf(String menuItemName, int quantity) {
        MenuItem menuItem = new MenuItem(menuItemName, 10, 30);
        deliveryLocation = facade.selectDeliveryLocation("Templiers A")
                .orElseThrow(() -> new IllegalArgumentException("Delivery location not found"));

        try {
            if (selectedRestaurantId == null) {
                throw new IllegalStateException("No restaurant selected");
            }
            orderDeliveryTime = facade.reserveDeliveryTime(selectedRestaurantId, deliveryTime);
            currentOrder = facade.createOrder(selectedRestaurantId, userId, deliveryLocation.getId(), orderDeliveryTime);
            currentOrderId = currentOrder.getId();
            facade.addItems(currentOrderId, String.valueOf(menuItem), quantity);
        } catch (IllegalStateException | MenuItemNotFoundException e) {
            currentOrder = null;
        }
    }

    @When("the user tries to add a null item with a quantity of {int}")
    public void theUserTriesToAddANullItemWithAQuantityOf(int quantity) {
        deliveryLocation = facade.selectDeliveryLocation("Templiers A")
                .orElseThrow(() -> new IllegalArgumentException("Delivery location not found"));
        try {
            if (selectedRestaurantId == null) {
                throw new IllegalStateException("No restaurant selected");
            }
            orderDeliveryTime = facade.reserveDeliveryTime(selectedRestaurantId, deliveryTime);
            currentOrder = facade.createOrder(selectedRestaurantId, userId, deliveryLocation.getId(), orderDeliveryTime);
            currentOrderId = currentOrder.getId();
            facade.addItems(currentOrderId, null, quantity);
        } catch (IllegalStateException | MenuItemNotFoundException e) {
            currentOrder = null;
        }
    }

    @Then("the order remains empty")
    public void theOrderRemainsEmpty() {
        assertTrue(currentOrder == null || currentOrder.getOrderItems().isEmpty());
    }

    @Given("the following menu items are available at {string}:")
    public void theFollowingMenuItemsAreAvailableAt(String restaurantName, List<Map<String, String>> menuItems) {
        // Implement logic to set available menu items for a restaurant
    }

    @Given("the user selects as delivery time next Monday at {string}")
    public void theUserSelectsAsDeliveryTimeNextMondayAt(String deliveryTimeStr) {
        if (!LocalDate.now().with(DayOfWeek.MONDAY).isAfter(LocalDate.now())) {
            deliveryTime = LocalDateTime.now().with(DayOfWeek.MONDAY).with(LocalTime.parse(deliveryTimeStr)).plusDays(7);
        } else {
            deliveryTime = LocalDateTime.now().with(DayOfWeek.MONDAY).with(LocalTime.parse(deliveryTimeStr));
        }
    }

    @When("the user adds the following items to the order:")
    public void theUserAddsTheFollowingItemsToTheOrder(List<Map<String, String>> items) {
        for (Map<String, String> item : items) {
            String menuItemName = item.get("name");
            int quantity = Integer.parseInt(item.get("quantity"));
            deliveryLocation = facade.selectDeliveryLocation("Templiers A")
                    .orElseThrow(() -> new IllegalArgumentException("Delivery location not found"));
            if (selectedRestaurantId == null) {
                throw new IllegalStateException("No restaurant selected");
            }
            orderDeliveryTime = facade.reserveDeliveryTime(selectedRestaurantId, deliveryTime);
            currentOrder = facade.createOrder(selectedRestaurantId, userId, deliveryLocation.getId(), orderDeliveryTime);
            currentOrderId = currentOrder.getId();
            selectedRestaurant.getMenu()
                    .stream()
                    .filter(menuItem -> menuItem.getName().equals(menuItemName))
                    .findFirst()
                    .ifPresentOrElse(
                            menuItem -> {
                                try {
                                    facade.addItems(currentOrderId, menuItem.getId(), quantity);
                                } catch (MenuItemNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                            },
                            () -> {
                                throw new IllegalArgumentException("Menu item not found");
                            }
                    );

        }
    }

    @Then("the total price of the order should be {double} €")
    public void theTotalPriceOfTheOrderShouldBe(double expectedTotalPrice) {
        assertEquals(expectedTotalPrice, currentOrder.getTotalAmount(), 0.01);
    }


}