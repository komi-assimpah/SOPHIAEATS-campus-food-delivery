package fr.unice.polytech.stepDefs.discountStrategy;

import fr.unice.polytech.Facade;
import fr.unice.polytech.application.port.IRestaurantRepository;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantService;
import fr.unice.polytech.domain.exceptions.MenuItemNotFoundException;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.discountstrategy.PercentageDiscount;
import fr.unice.polytech.domain.models.user.User;
import fr.unice.polytech.infrastructure.repository.inmemory.RestaurantRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PercentageStrategyStepDefs {

    private Facade facade;
    private String orderId;
    public IRestaurantService restaurantService;
    private Restaurant restaurant;
    private User customer = new User("Sara Polytech", "sarapolytech@gmail.com", "password");
    private MenuItem Caesar = new MenuItem("Pizza", 10.0f, 1);
    private MenuItem Fries = new MenuItem("Pasta", 12.0f, 1);
    private PercentageDiscount percentageDiscount;
    private Order order;
    IRestaurantRepository restaurantRepository = new RestaurantRepository();
    private final String MACDO_RESTAURANT_ID = "1";
    private final String JOHN_DOE_USER_ID = "1";

    public PercentageStrategyStepDefs() {
        this.facade = new Facade();
        restaurantService = facade.getRestaurantService();
        restaurantService.setRestaurantRepository(restaurantRepository);

    }

    @Given("a restaurant already named {string} exists with the following menu:")
    public void a_restaurant_already_named_exists_with_the_following_menu(String restaurantName, List<Map<String, String>> menuItemsData) {
        restaurant = restaurantService.findByName(restaurantName)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        for (Map<String, String> data : menuItemsData) {
            String itemName = data.get("name");
            double price = Double.parseDouble(data.get("price"));
            int prepTime = Integer.parseInt(data.get("preparation_time_in_mins"));

            MenuItem menuItem = new MenuItem(itemName, price, prepTime);
            restaurant.addMenuItem(menuItem);
        }
    }

    @Given("a restaurant already named \"McDonald's\" exists")
    public void thisRestaurantAlreadyExists() {
        restaurant = restaurantService.findByName("McDonald's")
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
    }

    @Given("restaurant already has a menu with menuItems")
    public void restaurantAlreadyHasMenuWithMenuItems() {
        // Restaurant repo contains dummy data
        // Nothing to do here
        restaurant = restaurantService.findByName("McDonald's")
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        Caesar = restaurant.getMenu().stream().filter(menuItem -> menuItem.getName().equals("Caesar")).findFirst().orElse(null);
        Fries = restaurant.getMenu().stream().filter(menuItem -> menuItem.getName().equals("Fries")).findFirst().orElse(null);
    }

    @Given("an order with 11 {string} with the price of 5.0 each")
    public void anOrderWith11Items(String menuItemName) throws MenuItemNotFoundException {
        LocalDateTime deliveryTime = LocalDateTime.now()
                .with(DayOfWeek.valueOf("MONDAY"))
                .withHour(13)
                .withMinute(0)
                .plusMinutes(1);

        MenuItem item = restaurant.getMenu().stream().filter(menuItem -> menuItem.getName().equals(menuItemName)).findFirst().orElse(null);
        order = facade.getOrderCoordinator().createOrder(MACDO_RESTAURANT_ID, JOHN_DOE_USER_ID, "1", deliveryTime);
        facade.getOrderCoordinator().addMenuItemToOrder(JOHN_DOE_USER_ID, item, 11);
    }

    @And("the restaurant has applied a {int}% discount for orders with {int}+ items")
    public void theRestaurantHasAppliedADiscountForOrdersWithItems(int percentage, int itemQuantity) {
        percentageDiscount = new PercentageDiscount(percentage, itemQuantity);
        restaurant.clearDiscountStrategies();
        restaurantService.addDiscountStrategy(restaurant.getId(), percentageDiscount);
    }

    @When("the customer confirms the order")
    public void theCustomerConfirmsTheOrder() {
        facade.placeOrder(order.getId());
        facade.payOrder(order.getId());
    }

    @Then("a percentage discount is applied")
    public void aPercentageDiscountIsApplied() {
        assertEquals(5.5, order.getUser().getBalance());
    }

    @And("a discount message is displayed: {string}")
    public void aDiscountMessageIsDisplayed(String message) {
        assertEquals("A discount of 5.5 has been applied. Thanks to the restaurant manager.", message);
    }

    @And("customers discount is added to customer's balance")
    public void customersDiscountIsAddedToCustomersBalance() {
        Order order = facade.getOrderCoordinator().getOrderById(orderId);
        assertEquals(5.5, order.getUser().getBalance());
    }
}
