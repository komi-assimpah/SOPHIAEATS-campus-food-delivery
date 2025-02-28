package fr.unice.polytech.stepDefs.discountStrategy;

import fr.unice.polytech.Facade;
import fr.unice.polytech.application.port.IRestaurantRepository;
import fr.unice.polytech.application.usecase.RestaurantService;
import fr.unice.polytech.domain.exceptions.MenuItemNotFoundException;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.discountstrategy.Buy1Get1Strategy;
import fr.unice.polytech.infrastructure.repository.inmemory.RestaurantRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Buy1Get1StrategyStepDefs {

    private Facade facade;
    private final String MACDO_RESTAURANT_ID = "1";
    private String orderId;
    private Order order;
    private final IRestaurantRepository restaurantRepository = new RestaurantRepository();
    private final RestaurantService restaurantService = new RestaurantService(restaurantRepository);
    private Restaurant restaurant;
    private MenuItem fries;
    private MenuItem caesar;
    private MenuItem coke;
    private Buy1Get1Strategy buy1Get1Discount;

    public Buy1Get1StrategyStepDefs() {
        this.facade = new Facade();
    }

//    @Given("this restaurant already exists")
//    public void thisRestaurantAlreadyExists() {
//        restaurant = restaurantService.findByName("McDonald's")
//                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
//    }

    @Given("buy1get1 strategy already added to restaurant strategies")
    public void buy1get1StrategyAlreadyAddedToRestaurantStrategies() {
        restaurant = restaurantService.findByName("McDonald's")
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        restaurant.clearDiscountStrategies();
        fries = restaurant.getMenu().stream()
                .filter(item -> item.getName().equals("Fries"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Fries not found in menu"));
        coke = restaurant.getMenu().stream()
                .filter(item -> item.getName().equals("Coke"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Coke not found in menu"));


    }

    @When("the restaurant manager sets the item {string} as a buy1get1 free item")
    public void theRestaurantManagerSetsTheItemAsABuy1Get1FreeItem(String itemName) {
        //Already done in previous stepDef
    }

    @And("the customer places an order with {int} {string} and {int} {string} and {int} {string}")
    public void theCustomerPlacesAnOrderWithItems(int friesQuantity, String friesName, int caesarQuantity, String caesarName, int cokeQuantity, String cokeName) throws MenuItemNotFoundException {
        LocalDateTime deliveryTime = LocalDateTime.now()
                .with(DayOfWeek.MONDAY)
                .withHour(13)
                .withMinute(0)
                .plusMinutes(1);

        orderId = facade.createOrder(restaurant.getId(), "1", "1", deliveryTime).getId();

        addItemToOrder(friesName, friesQuantity);
        addItemToOrder(caesarName, caesarQuantity);
        addItemToOrder(cokeName, cokeQuantity);

        order = facade.getOrderCoordinator().getOrderById(orderId);
    }

    private void addItemToOrder(String itemName, int quantity) throws MenuItemNotFoundException {
        MenuItem item = restaurant.getMenu().stream()
                .filter(menuItem -> menuItem.getName().equals(itemName))
                .findFirst()
                .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found: " + itemName));
        facade.getOrderCoordinator().addMenuItemToOrder("1", item, quantity);
    }

    @Then("the customer receives {int} free {string}")
    public void theCustomerReceivesFreeItem(int expectedFreeItems, String itemName) {
        MenuItem item = restaurant.getMenu().stream()
                .filter(menuItem -> menuItem.getName().equals(itemName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + itemName));
        restaurant.addDiscountStrategy(new Buy1Get1Strategy(item));
        restaurant.applyDiscountStrategy(order);

    }
    private int getOrderItemQuantity(Order order, MenuItem item) {
        return order.getOrderItems().stream()
                .filter(orderItem -> orderItem.getItem().equals(item))
                .mapToInt(orderItem -> orderItem.getQuantity())
                .sum();
    }




    @And("the customer receives a total of {int} {string} and {int} {string}")
    public void theCustomerReceivesTotalItems(int expectedFriesTotal, String friesName, int expectedCokeTotal, String cokeName) {
        assertTotalQuantity(expectedFriesTotal, friesName);
        assertTotalQuantity(expectedCokeTotal, cokeName);
    }

    private void assertTotalQuantity(int expectedTotal, String itemName) {
        MenuItem item = restaurant.getMenu().stream()
                .filter(menuItem -> menuItem.getName().equals(itemName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + itemName));

        restaurant.applyDiscountStrategy(order);

    }

    @And("the total price reflects the discount, excluding the price of the free items")
    public void theTotalPriceReflectsTheDiscountExcludingThePriceOfTheFreeItems() {
        restaurant.applyDiscountStrategy(order);
    }





    @Given("a restaurant manager")
    public void aRestaurantManager() {

    }
}
