package fr.unice.polytech.stepDefs.order;

import fr.unice.polytech.Facade;
import fr.unice.polytech.domain.exceptions.MenuItemNotFoundException;
import fr.unice.polytech.domain.models.delivery.DeliveryLocation;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.order.OrderStatus;
import fr.unice.polytech.domain.models.payment.PaymentDetails;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import fr.unice.polytech.domain.models.user.User;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.Assert.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Logger;

public class CapacityManagementSteps {
    private final List<Restaurant> restaurants = new ArrayList<>();
    private List<Restaurant> availableRestaurants = new ArrayList<>();
    private Restaurant selectedRestaurant;
    private Order currentOrder;
    private final Facade facade = new Facade();
    private List<LocalDateTime> availableDeliveryTimes;
    private LocalDateTime deliveryTime;
    private List<MenuItem> availableItems;
    private User user;

    @Given("these restaurants exist:")
    public void the_following_restaurants_exist(List<Map<String, String>> restaurants) {
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

    @Given("the targeted time is next Tuesday at {string}")
    public void theTargetedTimeIsNextTuesdayAt(String deliveryTimeStr) {
        if (!LocalDate.now().with(DayOfWeek.TUESDAY).isAfter(LocalDate.now())) {
            deliveryTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.parse(deliveryTimeStr)).plusDays(7);
        } else {
            deliveryTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.parse(deliveryTimeStr));
        }
    }

    @And("the items are available in the restaurant {string}:")
    public void theItemsAreAvailableInTheFollowingRestaurants(String restaurantName, List<Map<String, String>> items) {
        selectedRestaurant = facade.getRestaurantByName(restaurantName);
        for (Map<String, String> data : items) {
            MenuItem item = new MenuItem(data.get("menu_item_name"), Double.parseDouble(data.get("price")), Integer.parseInt(data.get("preparation_time")));
            Logger.getGlobal().info(item.getName());
            selectedRestaurant.addMenuItem(item);
        }

    }

    @When("the user requests the list of available restaurants")
    public void the_user_requests_the_list_of_available_restaurants() {
        LocalDateTime targetedTime;
        if (!LocalDate.now().with(DayOfWeek.TUESDAY).isAfter(LocalDate.now())) {
            targetedTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.of(14, 0, 0)).plusDays(7);
        } else {
            targetedTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.of(14, 0, 0));
        }
        availableRestaurants = facade.getAvailableRestaurants(targetedTime);
        Logger.getGlobal().info(availableRestaurants.toString());
    }

    @Then("the list should contain the following restaurants:")
    public void the_list_should_contain_the_following_restaurants(List<Map<String, String>> restaurants) {
        for (Map<String, String> data : restaurants) {
            if (availableRestaurants.stream().filter(restaurant -> restaurant.getName().equals(data.get("name"))).count() == 0) {
                Logger.getGlobal().info("Restaurant not found: " + data.get("name"));
            }
            assertTrue(availableRestaurants.stream().anyMatch(restaurant -> restaurant.getName().equals(data.get("name"))));
        }
    }

    @Given("the user chooses the restaurant {string}")
    public void the_user_selects_the_restaurant(String restaurantName) {
        selectedRestaurant = restaurants.stream()
                .filter(r -> r.getName().equals(restaurantName))
                .findFirst()
                .orElse(null);
    }
    @When("the user requests the available delivery times for {string} next Tuesday at {string}")
    public void the_user_requests_the_available_delivery_times_for_next_tuesday_at(String restaurantName, String deliveryTimeStr) {
        // Assume delivery times are fetched based on restaurant and time
        LocalDateTime expectedTime;
        if (!LocalDate.now().with(DayOfWeek.TUESDAY).isAfter(LocalDate.now())) {
            expectedTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.parse(deliveryTimeStr)).plusDays(7);
        } else {
            expectedTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.parse(deliveryTimeStr));
        }
        availableDeliveryTimes = facade.getAvailableDeliveryTime(restaurantName, expectedTime);
        assertNotNull(availableDeliveryTimes);
    }

    @Then("the list should contain the following delivery times:")
    public void the_list_should_contain_the_following_delivery_times(List<Map<String, String>> availableTimes) {
        List<LocalTime> times = new ArrayList<>();
        for (Map<String, String> data : availableTimes) {
            times.add(LocalTime.parse(data.get("time")));
        }
        availableDeliveryTimes.forEach(time -> {
            assertTrue(times.contains(time.toLocalTime()));
        });
    }

    @Given("the user chooses the delivery time next Tuesday at {string} and the restaurant {string}")
    public void theUserChoosesTheDeliveryTimeAndTheRestaurant(String deliveryTime, String restaurantName) {
        selectedRestaurant = facade.getRestaurantByName(restaurantName);
        LocalDateTime expectedTime;
        if (!LocalDate.now().with(DayOfWeek.TUESDAY).isAfter(LocalDate.now())) {
            expectedTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.of(12,10, 0)).plusDays(7);
        } else {
            expectedTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.of(12, 10, 0));
        }
        facade.getAvailableDeliveryTime(selectedRestaurant.getName(), expectedTime)
                .stream()
                .filter(time -> time.equals(
                        LocalDateTime.now().with(DayOfWeek.TUESDAY).with(
                                LocalTime.parse(deliveryTime))))
                .findFirst()
                .ifPresent(time -> this.deliveryTime = time);
    }

    @When("the user reserves as delivery time next Tuesday at {string}")
    public void the_user_reserves_the_delivery_time(String deliveryTime) {
        user = facade.createUser("Jackie Chan", "chan@gmail.com", "password");
        String userId = user.getId();
        DeliveryLocation deliveryLocation = facade.selectDeliveryLocation("Templiers A")
                .orElseThrow(() -> new IllegalArgumentException("Delivery location not found"));
        LocalTime time = LocalTime.parse(deliveryTime);
        LocalDateTime deliveryDateTime;
        if (!LocalDate.now().with(DayOfWeek.TUESDAY).isAfter(LocalDate.now())) {
            deliveryDateTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(time).plusDays(7);
        } else {
            deliveryDateTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(time);
        }
        LocalDateTime orderDeliveryTime = facade.reserveDeliveryTime(selectedRestaurant.getId(), deliveryDateTime);
        currentOrder = facade.createOrder(selectedRestaurant.getId(), userId, deliveryLocation.getId(), orderDeliveryTime);
    }

    @Then("a new order should be created with status {string}")
    public void a_new_order_should_be_created_with_status(String status) {
        assertEquals(status, currentOrder.getStatus().toString());
    }

    @Then("the order's delivery time should be next Tuesday at {string}")
    public void the_order_s_delivery_time_should_be(String deliveryTime) {
        LocalDateTime expectedTime;
        if (!LocalDate.now().with(DayOfWeek.TUESDAY).isAfter(LocalDate.now())) {
            expectedTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.parse(deliveryTime)).plusDays(7);
        } else {
            expectedTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.parse(deliveryTime));
        }
        assertEquals(expectedTime, currentOrder.getDeliveryTime());
    }

    @Then("the remaining capacity for {string} should be {int}")
    public void the_remaining_capacity_for_should_be(String restaurantName, int remainingCapacity) {
        selectedRestaurant = facade.getRestaurantByName(restaurantName);
        selectedRestaurant.getSchedules().forEach(schedule -> {
            if (schedule.getDay().equals(LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.of(14, 0, 0)).getDayOfWeek())) {
                assertEquals(remainingCapacity, schedule.getCapacityOfProduction());
            }
        });
    }

    @Given("the order is created with restaurant {string}")
    public void the_order_is_created_with_restaurant(String restaurantName) {
        selectedRestaurant = facade.getRestaurantByName(restaurantName);
        LocalDateTime deliveryDateTime;
        if (!LocalDate.now().with(DayOfWeek.TUESDAY).isAfter(LocalDate.now())) {
            deliveryDateTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.of(14, 0, 0)).plusDays(7);
        } else {
            deliveryDateTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.of(14, 0, 0));
        }
        user = facade.createUser("Jackie Chan", "chan@gmail.com", "password");
        DeliveryLocation deliveryLocation = facade.selectDeliveryLocation("Templiers A")
                .orElseThrow(() -> new IllegalArgumentException("Delivery location not found"));
        currentOrder = facade.createOrder(selectedRestaurant.getId(), user.getId(), deliveryLocation.getId(), deliveryDateTime);
    }

    @When("the user requests the available menu items for {string} next Tuesday at {string}")
    public void the_user_requests_the_available_menu_items_for_at(String restaurantName, String time) {
        Restaurant restaurant = facade.getRestaurantByName(restaurantName);
        LocalDateTime deliveryDateTime;
        if (!LocalDate.now().with(DayOfWeek.TUESDAY).isAfter(LocalDate.now())) {
            deliveryDateTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.parse(time)).plusDays(7);
        } else {
            deliveryDateTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.parse(time));
        }
        Logger.getGlobal().info("Delivery time: " + deliveryTime);
        availableItems = facade.getAvailableMenuItems(restaurant.getId(), deliveryDateTime);
    }

    @Then("the list of available menu items should include:")
    public void the_list_of_available_menu_items_should_include(List<Map<String, String>> items) {
        for (Map<String, String> data : items) {
            assertTrue(availableItems.stream().anyMatch(item -> item.getName().equals(data.get("menu_item_name"))));
        }

    }

    @Given("the user adds the following menu items to the order:")
    public void the_user_adds_the_following_menu_items_to_the_order(List<Map<String, String>> items) {
        LocalDateTime deliveryDateTime;
        if (!LocalDate.now().with(DayOfWeek.TUESDAY).isAfter(LocalDate.now())) {
            deliveryDateTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.of(14, 0, 0)).plusDays(7);
        } else {
            deliveryDateTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.of(14, 0, 0));
        }
        availableItems = facade.getAvailableMenuItems(selectedRestaurant.getId(), deliveryDateTime);
        user = facade.createUser("Jackie Chan", "chan@gmail.com", "password");
        DeliveryLocation deliveryLocation = facade.selectDeliveryLocation("Templiers A")
                .orElseThrow(() -> new IllegalArgumentException("Delivery location not found"));
        currentOrder = facade.createOrder(selectedRestaurant.getId(), user.getId(), deliveryLocation.getId(), deliveryDateTime);
        for (Map<String, String> data : items) {
            MenuItem item = availableItems.stream()
                    .filter(menuItem -> menuItem.getName().equals(data.get("name")))
                    .findFirst()
                    .orElse(null);
            if (item != null) {
                try {
                    facade.addItems(currentOrder.getId(), item.getId(), Integer.parseInt(data.get("quantity")));
                } catch (MenuItemNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @When("the user places the order")
    public void the_user_places_the_order() {
        boolean success = facade.placeOrder(currentOrder.getId());
        if (success) {
            currentOrder.setStatus(OrderStatus.CONFIRMED);
        }
    }

    @Then("the order status should be {string}")
    public void the_order_status_should_be(String status) {
        assertEquals(status, currentOrder.getStatus().toString());
    }

    @Given("the order has been placed and the order status is {string}")
    public void the_order_has_been_placed_with_the_following_details(String status) {
        LocalDateTime deliveryDateTime;
        if (!LocalDate.now().with(DayOfWeek.TUESDAY).isAfter(LocalDate.now())) {
            deliveryDateTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.of(14, 0, 0)).plusDays(7);
        } else {
            deliveryDateTime = LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.of(14, 0, 0));
        }
        user = facade.createUser("Jackie Chan", "chan@gmail.com", "password");
        DeliveryLocation deliveryLocation = facade.selectDeliveryLocation("Templiers A")
                .orElseThrow(() -> new IllegalArgumentException("Delivery location not found"));
        PaymentDetails details = new PaymentDetails("1234567890", "12/24", "123");
        user.addPaymentMethod(details);
        currentOrder = facade.createOrder(selectedRestaurant.getId(), user.getId(), deliveryLocation.getId(), deliveryDateTime);
        currentOrder.setStatus(OrderStatus.valueOf(status));
    }

    @When("the user pays for the order")
    public void the_user_pays_for_the_order() {
        facade.payOrder(currentOrder.getId());
    }

    @Then("the payment should be successful")
    public void the_payment_should_be_successful() {
        assertTrue(facade.placeOrder(currentOrder.getId()));
    }

}