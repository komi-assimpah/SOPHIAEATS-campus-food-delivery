package fr.unice.polytech.stepDefs.restaurant;

import fr.unice.polytech.application.port.IRestaurantRepository;
import fr.unice.polytech.application.usecase.RestaurantService;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.infrastructure.repository.inmemory.RestaurantRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RestaurantMenuSteps {

    private Restaurant restaurant;
    private String errorMessage;
    private RestaurantService restaurantService;
    private IRestaurantRepository restaurantRepository;

    public RestaurantMenuSteps() {
        restaurantRepository = new RestaurantRepository();
        restaurantService = new RestaurantService(restaurantRepository);
    }

    @Given("a restaurant named {string} exists with no menu items")
    public void a_restaurant_named_exists_with_no_menu_items(String restaurantName) {
        restaurant = restaurantService.createRestaurant(restaurantName, "930 Route des Colles", "06410", "Biot", "France");
        assertTrue(restaurant.getMenu().isEmpty());
    }

    @When("the Restaurant Manager adds a menu item {string} with a price of {double} and preparation time of {int} seconds")
    public void the_restaurant_manager_adds_a_menu_item_with_a_price_of_and_preparation_time_of_seconds(String name, Double price, Integer prepTime) {
        try {
            restaurant.addMenuItem(new MenuItem(name, price, prepTime));
        } catch (IllegalArgumentException e) {
            errorMessage = e.getMessage();
        }
    }

    @Then("the restaurant menu should contain exactly {int} item(s):")
    public void the_restaurant_menu_should_contain_exactly(int nbOfItems, List<Map<String, String>> expectedMenuTable) {
        assertEquals(nbOfItems, restaurant.getMenu().size());
        validateMenuItems(expectedMenuTable);
    }

    @Given("a restaurant named {string} exists with the following menu:")
    public void a_restaurant_named_exists_with_the_following_menu(String restaurantName, List<Map<String, String>> menuTable) {
        restaurant = restaurantService.createRestaurant(restaurantName, "930 Route des Colles", "06410", "Biot", "France");
        for (Map<String, String> row : menuTable) {
            String name = row.get("Name");
            double price = Double.parseDouble(row.get("Price"));
            int preparationTime = Integer.parseInt(row.get("Preparation Time"));
            restaurant.addMenuItem(new MenuItem(name, price, preparationTime));
        }
    }

    @When("the Restaurant Manager removes the menu item {string}")
    public void the_restaurant_manager_removes_the_menu_item(String menuItemName) {
        try {
            restaurant.removeMenuItem(menuItemName);
        } catch (Exception e) {
            errorMessage = e.getMessage();
        }
    }

    @When("the Restaurant Manager attempts to remove a menu item {string}")
    public void the_restaurant_manager_attempts_to_remove_a_menu_item(String menuItemName) {
        try {
            restaurant.removeMenuItem(menuItemName);
        } catch (Exception e) {
            errorMessage = e.getMessage();
        }
    }

    @Then("the system should return an error message {string}")
    public void the_system_should_return_an_error_message(String expectedErrorMessage) {
        assertEquals(expectedErrorMessage, errorMessage);
    }

    @When("any Internet User views the restaurant menu")
    public void any_internet_user_views_the_restaurant_menu() {
        // No action needed
    }

    @Then("the restaurant menu should contain the following items:")
    public void the_restaurant_menu_should_contain_the_following_items(List<Map<String, String>> expectedMenuTable) {
        validateMenuItems(expectedMenuTable);
    }

    @When("the Restaurant Manager attempts to add a menu item {string} with a price of {double} and preparation time of {int} seconds")
    public void the_restaurant_manager_attempts_to_add_a_menu_item_with_a_price_of_and_preparation_time_of_seconds(String string, Double double1, Integer int1) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    private void validateMenuItems(List<Map<String, String>> expectedMenuTable) {
        List<MenuItem> menu = restaurant.getMenu();
        assertEquals(expectedMenuTable.size(), menu.size());

        for (Map<String, String> expectedRow : expectedMenuTable) {
            String expectedName = expectedRow.get("Name");
            double expectedPrice = Double.parseDouble(expectedRow.get("Price"));
            int expectedPrepTime = Integer.parseInt(expectedRow.get("Preparation Time"));

            MenuItem expectedItem = new MenuItem(expectedName, expectedPrice, expectedPrepTime);
            assertTrue(menu.contains(expectedItem));
        }
    }
}
