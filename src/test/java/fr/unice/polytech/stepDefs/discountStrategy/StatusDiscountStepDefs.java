package fr.unice.polytech.stepDefs.discountStrategy;

import fr.unice.polytech.Facade;
import fr.unice.polytech.application.port.IRestaurantRepository;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantService;
import fr.unice.polytech.domain.exceptions.MenuItemNotFoundException;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.order.OrderStatus;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.discountstrategy.StatusDiscount;
import fr.unice.polytech.domain.models.user.User;
import fr.unice.polytech.domain.models.user.UserStatus;
import fr.unice.polytech.infrastructure.repository.inmemory.RestaurantRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StatusDiscountStepDefs {

    private final Facade facade;
    private final IRestaurantService restaurantService;
    private Restaurant restaurant;
    private IRestaurantRepository restaurantRepository = new RestaurantRepository();
    private User customer = new User("Sara Polytech", "sarapolytech@gmail.com", "password");
    private Order order;
    private Order order2;
    private StatusDiscount statusDiscount;
    private Exception exception;

    public StatusDiscountStepDefs() {
        this.facade = new Facade();
        restaurantService = facade.getRestaurantService();
        restaurantService.setRestaurantRepository(restaurantRepository);

    }


    @Given("a status discount strategy for CAMPUS_STUDENT with a 15% discount is added to the restaurant strategies")
    public void aStatusDiscountStrategyForCAMPUS_STUDENTWith15PercentDiscountIsAdded() {
        restaurant = restaurantService.findByName("McDonald's")
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        statusDiscount = new StatusDiscount(UserStatus.CAMPUS_STUDENT, 15);
        restaurant.addDiscountStrategy(statusDiscount);
    }

    @Given("this restaurant already exists")
    public void thisRestaurantAlreadyExists() {

        restaurant = restaurantService.findByName("McDonald's")
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
    }

    @Given("restaurant already has a menu with menuItems already")
    public void restaurantAlreadyHasMenuWithMenuItems() {
        // Restaurant repo contains dummy data
        // Nothing to do here

    }

    @Given("an order with 3 items placed by a customer with the CAMPUS_STUDENT status")
    public void anOrderWith3ItemsPlacedByCustomerWithCAMPUS_STUDENTStatus() throws MenuItemNotFoundException {
        MenuItem Caesar = restaurant.getMenu().stream().filter(menuItem -> menuItem.getName().equals("Caesar")).findFirst().orElse(null);
        MenuItem Fries = restaurant.getMenu().stream().filter(menuItem -> menuItem.getName().equals("Fries")).findFirst().orElse(null);
        String day = "monday";
        String time = "12:00";
        LocalDate date = DayOfWeek.valueOf(day.toUpperCase()) == DayOfWeek.MONDAY ? LocalDate.of(2024, 10, 14) : LocalDate.of(2024, 10, 15);
        LocalTime localTime = LocalTime.parse(time);
        //LocalDateTime deliveryTime = LocalDateTime.of(date, localTime);
        LocalDateTime LocalDateTime = java.time.LocalDateTime.now();
        LocalTime onePM = LocalTime.NOON;

        LocalDateTime deliveryTime = LocalDateTime
                .with(DayOfWeek.valueOf(day.toUpperCase()))
                .withHour(13)
                .withMinute(0)
                .plusMinutes(1);

        // Combine both to get a LocalDateTime
        LocalDateTime noon = java.time.LocalDateTime.of(date, onePM);
        MenuItem pizza = restaurant.getMenu().getFirst();
        order = facade.getOrderCoordinator().createOrder("1", "1", "1", deliveryTime);
        facade.getOrderCoordinator().addMenuItemToOrder("1", Caesar, 3);
        facade.payOrder(order.getId());
        facade.placeOrder(order.getId());
        order.setStatus(OrderStatus.CONFIRMED);
        order.getUser().setType(UserStatus.CAMPUS_STUDENT);
    }

    @Given("an order with 5 items placed by a customer with the LOYAL_CUSTOMER status")
    public void anOrderWith5ItemsPlacedByCustomerWithLOYAL_CUSTOMERStatus() throws MenuItemNotFoundException {
        MenuItem Caesar = restaurant.getMenu().stream().filter(menuItem -> menuItem.getName().equals("Caesar")).findFirst().orElse(null);
        MenuItem Fries = restaurant.getMenu().stream().filter(menuItem -> menuItem.getName().equals("Fries")).findFirst().orElse(null);
        String day = "monday";
        String time = "12:00";
        LocalDate date = DayOfWeek.valueOf(day.toUpperCase()) == DayOfWeek.MONDAY ? LocalDate.of(2024, 10, 14) : LocalDate.of(2024, 10, 15);
        LocalTime localTime = LocalTime.parse(time);
        //LocalDateTime deliveryTime = LocalDateTime.of(date, localTime);
        LocalDateTime LocalDateTime = java.time.LocalDateTime.now();
        LocalTime onePM = LocalTime.NOON;

        LocalDateTime deliveryTime = LocalDateTime
                .with(DayOfWeek.valueOf(day.toUpperCase()))
                .withHour(13)
                .withMinute(0)
                .plusMinutes(1);

        // Combine both to get a LocalDateTime
        LocalDateTime noon = java.time.LocalDateTime.of(date, onePM);
        MenuItem pizza = restaurant.getMenu().getFirst();
        order2 = facade.getOrderCoordinator().createOrder("1", "1", "1", deliveryTime);
        facade.getOrderCoordinator().addMenuItemToOrder("1", Caesar, 5);
        order2.getUser().setType(UserStatus.LOYAL_CUSTOMER);
        facade.payOrder(order2.getId());
        facade.placeOrder(order2.getId());
        restaurant.applyDiscountStrategy(order2);

    }


    @When("the customer confirms the order as a student")
    public void theCustomerConfirmsTheOrderAsAStudent() {

        //Done in previous methods

    }

    @Then("a 15% status discount is applied to the order")
    public void a15PercentStatusDiscountIsAppliedToTheOrder() {
        double expectedDiscount = order.getTotalAmount() * 0.15;
        assertEquals(expectedDiscount, order.getUser().getBalance(), "Discount amount applied should match the expected value.");
    }

    @And("a discount message is displayed: {string} for CAMPUS_STUDENT")
    public void aDiscountMessageIsDisplayed(String message) {
        String expectedMessage = "A 15% discount has been applied for CAMPUS_STUDENT.";
        assertEquals(expectedMessage, message);
    }

    @And("the customer's balance is updated with the discount amount")
    public void theCustomersBalanceIsUpdatedWithTheDiscountAmount() {
        System.out.println("Updated balance after discount: " + order.getUser().getBalance());
    }

    @Then("no discount should be applied")
    public void noDiscountShouldBeApplied() {
        assertEquals(0.0, order2.getUser().getBalance(), "No discount should be applied for non-eligible status.");
    }

    @And("the customer's balance remains the same")
    public void theCustomersBalanceRemainsTheSame() {
        System.out.println("Customer balance remains unchanged: " + order2.getUser().getBalance());
    }

    @And("a message is displayed: {string}")
    public void aMessageIsDisplayed(String message) {
        System.out.println(message);  // Display a message indicating no discount is available
    }

    @When("the restaurant manager tries to add a status discount strategy with a percentage greater than 100%")
    public void theRestaurantManagerTriesToAddAStatusDiscountWithInvalidPercentage() {
        exception = assertThrows(IllegalArgumentException.class, () -> {
            StatusDiscount invalidDiscount = new StatusDiscount(UserStatus.CAMPUS_STUDENT, 105);
            restaurant.addDiscountStrategy(invalidDiscount);
        });
    }

    @Then("an error message is displayed: {string}")
    public void anErrorMessageIsDisplayed(String expectedMessage) {
        assertEquals(expectedMessage, exception.getMessage(), "Error message should match expected message.");
    }
}
