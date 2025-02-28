package fr.unice.polytech.stepDefs.order;

import fr.unice.polytech.application.port.IRestaurantRepository;
import fr.unice.polytech.application.usecase.RestaurantService;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.user.User;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class SelectRestaurantStepDefs {

    private RestaurantService restaurantService;
    private IRestaurantRepository restaurantRepository;
    private Restaurant restaurant;
    private User user;

    public SelectRestaurantStepDefs() {
        restaurant = restaurantService.createRestaurant("bistoria", "930 Route des Colles", "06410", "Biot", "France");
        user = new User("john_doe", "johndoe@email.com","password");
    }


    List<MenuItem> menuItems = Arrays.asList(
            new MenuItem("Pizza Margherita", 14, 20),
            new MenuItem("Hamburger", 10, 10),
            new MenuItem("Sushi", 20, 30),
            new MenuItem("Salade", 12, 15),
            new MenuItem("Nuggets", 15, 15),
            new MenuItem("Tiramisu", 5, 15)
    );


    @Given("a customer is logged in")
    public void aCustomerIsLoggedIn() {
        this.user.setName("john_doe");
        this.user.setEmail("johndoe@gmail.com");
    }

    @When("a restaurant {string} is selected from the following list of available restaurants:")
    public void aRestaurantIsSelectedFromTheFollowingListOfAvailableRestaurants(String restaurantName, List<String> availableRestaurants) {
        restaurant.setName(restaurantName);
        restaurant.setMenu(menuItems);
        assertTrue(availableRestaurants.contains(restaurantName));
    }

    @Then("the list of available menus in this restaurant is displayed,")
    public void theListOfAvailableMenusInThisRestaurantIsDisplayed() {
        assertNotNull(restaurant.getMenu());
    }

    @And("the details of the menus include the price and preparation time:")
    public void theDetailsOfTheMenusIncludeThePriceAndPreparationTime(List<Map<String, String>> expectedItems) {
        for (Map<String, String> item : expectedItems) {
            List<MenuItem> menuItems = restaurant.getMenu();
            for (MenuItem menuItem : menuItems) {
                if (menuItem.getName().equals(item.get("nom"))) {
                    assertEquals(Double.parseDouble(item.get("prix")), menuItem.getPrice(), 0.01);
                    assertEquals(Integer.parseInt(item.get("temps_preparation")), menuItem.getPreparationTimeInMinutes());
                }
            }

        }
    }

}
