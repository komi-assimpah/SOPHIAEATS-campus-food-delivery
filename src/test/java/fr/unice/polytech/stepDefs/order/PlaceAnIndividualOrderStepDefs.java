package fr.unice.polytech.stepDefs.order;

import fr.unice.polytech.application.port.IPaymentService;
import fr.unice.polytech.application.port.IRestaurantRepository;
import fr.unice.polytech.application.port.IUserRepository;
import fr.unice.polytech.application.usecase.LocationService;
import fr.unice.polytech.application.usecase.OrderCoordinator;
import fr.unice.polytech.application.usecase.OrderService;
import fr.unice.polytech.application.usecase.RestaurantCapacityService;
import fr.unice.polytech.application.usecase.RestaurantScheduleManager;
import fr.unice.polytech.application.usecase.RestaurantService;
import fr.unice.polytech.application.usecase.UserService;
import fr.unice.polytech.application.usecase.interfaces.ILocationService;
import fr.unice.polytech.application.usecase.interfaces.IOrderService;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantCapacityService;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantScheduleManager;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantService;
import fr.unice.polytech.application.usecase.interfaces.IUserService;
import fr.unice.polytech.domain.models.delivery.DeliveryLocation;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.payment.PaymentDetails;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import fr.unice.polytech.domain.models.user.User;
import fr.unice.polytech.infrastructure.external.PaymentService;
import fr.unice.polytech.infrastructure.repository.inmemory.LocationRepository;
import fr.unice.polytech.infrastructure.repository.inmemory.OrderRepository;
import fr.unice.polytech.infrastructure.repository.inmemory.RestaurantRepository;
import fr.unice.polytech.infrastructure.repository.inmemory.UserRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.Mockito;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * We use the in memory repository for ease of testing
 * Since the in memory is cleared after each test, we don't need to mock every repository,
 * but for the sake of the example, we mock the user repository
 */
public class PlaceAnIndividualOrderStepDefs {

    private final OrderCoordinator orderCoordinator;
    private IRestaurantService restaurantService;
    private IRestaurantScheduleManager restaurantScheduleManager;
    private ILocationService locationService;
    private IUserRepository userRepositoryMock = Mockito.mock(UserRepository.class);
    private String userId;
    private User user;
    private Restaurant restaurant;
    private IUserService userService;
    private Order individualOrder;
    private Map<String, Restaurant> restaurants = new HashMap<>();

    public PlaceAnIndividualOrderStepDefs() {
        IRestaurantRepository restaurantRepository = new RestaurantRepository();
        restaurantService = new RestaurantService(restaurantRepository);
        restaurantScheduleManager = new RestaurantScheduleManager(restaurantRepository);
        userService = new UserService(userRepositoryMock);
        IOrderService orderService = new OrderService(new OrderRepository());
        IPaymentService paymentService = new PaymentService();
        IRestaurantCapacityService restaurantCapacityService = new RestaurantCapacityService();
        locationService = new LocationService(new LocationRepository());
        this.orderCoordinator = new OrderCoordinator(
                userService, restaurantService, restaurantCapacityService, orderService, paymentService, locationService
        );
    }

    @Given("the following restaurants exists:")
    public void the_following_restaurants_exists(List<Map<String, String>> restaurantData) {
        for (Map<String, String> data : restaurantData) {
            try {
                DayOfWeek dayOfWeek = DayOfWeek.valueOf(data.get("day").toUpperCase());
                LocalTime openingTime = LocalTime.parse(data.get("opening_hours"));
                LocalTime closingTime = LocalTime.parse(data.get("closing_hours"));
                int staffCount = Integer.parseInt(data.get("staff_count"));
                Schedule schedule = new Schedule(dayOfWeek, openingTime, closingTime, staffCount);

                Restaurant resto = restaurantService.createRestaurant(
                        data.get("name"), data.get("street"), data.get("city"), data.get("zip_code"), data.get("country")
                );
                restaurantScheduleManager.addSchedule(resto.getId(), schedule);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Given("the following menu items exist for the restaurant {string}:")
    public void the_following_menu_items_exist_for_the_restaurant(String restaurantName, List<Map<String, String>> menuItemsData) {
        restaurant = restaurantService.findByName(restaurantName)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        for (Map<String, String> data : menuItemsData) {
            String itemName = data.get("name");
            double price = Double.parseDouble(data.get("price"));
            int prepTime = Integer.parseInt(data.get("preparation_time"));

            MenuItem menuItem = new MenuItem(itemName, price, prepTime);
            restaurant.addMenuItem(menuItem);
        }
    }

    @Given("the user {string} with email {string} is logged in")
    public void the_user_is_logged_in(String userName, String email) {
        // Arrange
        User userMock = new User(userName, email, "password");
        PaymentDetails paymentDetails = new PaymentDetails("1234 5678 9012 3456", "12/24", "123");
        userMock.addPaymentMethod(paymentDetails);

        // Act
        Mockito.when(userRepositoryMock.findById(userMock.getId())).thenReturn(Optional.of(userMock));

        // Assert
        user = userService.getUserById(userMock.getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        userId = user.getId();
        assertEquals(email, user.getEmail());
    }

    Map<MenuItem, Integer> itemsOrdered;

    @Given("the user selected the restaurant {string} with the following menu items:")
    public void the_user_selected_the_restaurant_with_the_following_menu_items(String restaurantName, List<Map<String, String>> menuItemsData) {
        assertEquals(restaurantName, restaurant.getName());

        itemsOrdered = new HashMap<>();
        for (Map<String, String> data : menuItemsData) {
            String itemName = data.get("item");
            int quantity = Integer.parseInt(data.get("quantity"));
            restaurant.getMenu().stream()
                    .filter(menuItem -> menuItem.getName().equals(itemName))
                    .findFirst()
                    .ifPresent(menuItem -> itemsOrdered.put(menuItem, quantity));
        }
    }

    private DeliveryLocation deliveryLocation;

    @Given("the user selects the delivery location {string}")
    public void the_user_selects_the_delivery_location(String locationName) {
        deliveryLocation = locationService.getLocationByName(locationName)
                .orElseThrow(() -> new IllegalStateException("Location not found"));
    }

    private LocalDateTime deliveryTime;

    @Given("the user selects a delivery time for {string} at {int}:{int}")
    public void theUserSelectsADeliveryTimeForAt(String day, int hour, int minute) {
        deliveryTime = LocalDateTime.now()
                .with(DayOfWeek.valueOf(day.toUpperCase()))
                .withHour(hour)
                .withMinute(minute)
                .plusMinutes(1);
    }

    @Given("the order is created with the status {string}")
    public void the_order_is_created_with_the_status(String status) {
        individualOrder = orderCoordinator.createOrder(restaurant.getId(), userId, deliveryLocation.getId(), deliveryTime);
        assertEquals(status.toLowerCase(), individualOrder.getStatus().name().toLowerCase());
    }

    private boolean isPaymentSuccessful;

    @When("the user proceeds to payment")
    public void the_user_proceeds_to_payment() {
        isPaymentSuccessful = orderCoordinator.processPayment(individualOrder);
    }

    @Then("the payment is successfully processed")
    public void the_payment_is_successfully_processed() {
        if (!isPaymentSuccessful) {
            System.out.println("Payment failed");
        }
        assertTrue(isPaymentSuccessful);
    }

    @Then("the order status is changed to {string}")
    public void the_order_status_is_changed_to(String status) {
        assertEquals(status.toLowerCase(), individualOrder.getStatus().name().toLowerCase());
    }
}
