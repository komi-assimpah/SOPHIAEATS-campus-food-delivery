package fr.unice.polytech.stepDefs.groupOrder;

import fr.unice.polytech.Facade;
import fr.unice.polytech.application.dto.AddressDTO;
import fr.unice.polytech.application.dto.DeliveryLocationDTO;
import fr.unice.polytech.application.usecase.OrderCoordinator;
import fr.unice.polytech.application.usecase.interfaces.ILocationService;
import fr.unice.polytech.domain.exceptions.MenuItemNotFoundException;
import fr.unice.polytech.domain.models.order.OrderItem;
import fr.unice.polytech.domain.models.order.OrderStatus;
import fr.unice.polytech.domain.models.payment.PaymentDetails;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.groupOrder.GroupOrder;
import fr.unice.polytech.domain.models.groupOrder.GroupOrderStatus;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import fr.unice.polytech.domain.models.restaurant.discountstrategy.PercentageDiscount;
import fr.unice.polytech.domain.models.user.User;
import io.cucumber.java.en.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.junit.Assert.*;

public class ValidateGroupOrderStepDefs {

    private int ratio;
    private LocalDateTime timeBase;
    private Restaurant restaurant;
    private Order singleOrder;
    private Order singleOrder2;
    private GroupOrder groupOrder;
    private LocalDateTime deliveryTime;;
    private final String userID1;
    private final String userID2;
    private final String userID3;
    private final ILocationService locationService;
    private final Facade facade;
    private Exception exception;

    public ValidateGroupOrderStepDefs() {
        this.facade = new Facade();
        this.locationService = this.facade.getLocationService();
        User user = facade.createUser("John Doe", "johndoe@gmail.com", "password");
        user.addPaymentMethod(new PaymentDetails("1234567890123456", "123", "12/23"));
        this.userID1 = user.getId();
        user = facade.createUser("Jane Doe", "janedoe@gmail.com", "password");
        user.addPaymentMethod(new PaymentDetails("1234567890123456", "123", "12/23"));
        this.userID2 = user.getId();
        this.userID3 = facade.createUser("Jack Doe", "jackdoe@gmail.com", "password").getId();
        this.setDeliveryLocations();
        this.setRestaurant();
    }

    public void setDeliveryLocations() {
        List<Map<String, String>> deliveryLocations = List.of(
                Map.of("locationName", "Templiers", "street", "930 Route des Colles", "zipCode", "06410", "city", "Biot", "country", "France")
        );
        for (Map<String, String> location : deliveryLocations) {
            try {
                String locationName = location.get("locationName");
                String street = location.get("street");
                String zipCode = location.get("zipCode");
                String city = location.get("city");
                String country = location.get("country");

                AddressDTO addressDTO = new AddressDTO(street, zipCode, city, country);
                DeliveryLocationDTO deliveryLocationDTO = new DeliveryLocationDTO(locationName, addressDTO);
                this.locationService.addLocation(deliveryLocationDTO);
            } catch (Exception e) {
                Logger.getGlobal().info(e.getMessage());
            }
        }
    }

    private void setRestaurant() {
        List<MenuItem> menuItems = List.of(
                new MenuItem("Fries", 2.0, 6),
                new MenuItem("Big Mac", 5.0, 9)
        );
        List<Schedule> schedulesTable = List.of(
                new Schedule(DayOfWeek.MONDAY, LocalTime.of(6, 0), LocalTime.of(13, 59), 5),
                new Schedule(DayOfWeek.TUESDAY, LocalTime.of(6, 0), LocalTime.of(13, 59), 5),
                new Schedule(DayOfWeek.WEDNESDAY, LocalTime.of(6, 0), LocalTime.of(13, 59), 5),
                new Schedule(DayOfWeek.THURSDAY, LocalTime.of(6, 0), LocalTime.of(13, 59), 5),
                new Schedule(DayOfWeek.FRIDAY, LocalTime.of(6, 0), LocalTime.of(13, 59), 5),
                new Schedule(DayOfWeek.SATURDAY, LocalTime.of(6, 0), LocalTime.of(13, 59), 5),
                new Schedule(DayOfWeek.SUNDAY, LocalTime.of(6, 0), LocalTime.of(13, 59), 5)
        );

        this.facade.createRestaurant("McDonald's", "400 Route des Colles", "Biot", "06410", "France");
        this.restaurant = this.facade.findByName("McDonald's")
                .orElseThrow(() -> new IllegalStateException("Restaurant not found"));
        this.restaurant.setOpeningHours(schedulesTable);
        this.restaurant.setMenu(menuItems);
    }

    private Order placeOrder(String userID, MenuItem menuItem) throws MenuItemNotFoundException {
        Order order = this.facade.chooseRestaurant(userID, this.restaurant.getId());
        this.facade.addItems(order.getId(), menuItem.getId(), 5);
        assertEquals(order.getStatus(), OrderStatus.PENDING);
        this.facade.placeOrder(order.getId());
        assertEquals(order.getStatus(), OrderStatus.COMPLETED);
        return order;
    }

    public void delayRestautantAvailability() throws MenuItemNotFoundException {
        Order order = this.facade.createOrder(
                this.restaurant.getId(), this.userID3, this.locationService.getLocationByName("Templiers").orElseThrow(
                        () -> new IllegalArgumentException("Location not found")
                ).getId(), this.timeBase.plusHours((6)));
        this.facade.addItems(order.getId(), this.restaurant.getMenu().getFirst().getId(), 20);
        this.facade.placeOrder(order.getId());
    }

    @Given("a restaurant opening time {string}")
    public void aRestaurantOpeningTime(String openingTime) {
        this.timeBase = LocalTime.now().isBefore(LocalTime.parse(openingTime))
                ? LocalDateTime.now().with(LocalTime.parse(openingTime))
                : LocalDateTime.now().plusDays(1).with(LocalTime.parse(openingTime));
    }

    @Given("a delivery time {int} minutes from opening time provided")
    public void aDeliveryTimeFromOpeningTimeProvided(int deliveryDelay) {
        this.deliveryTime = this.timeBase.plusMinutes(deliveryDelay);
    }

    @Given("a closing group order with the status {string}")
    public void aClosingGroupOrderWithTheStatus(String groupOrderStatus) throws MenuItemNotFoundException {
        if (this.deliveryTime == null) this.deliveryTime = this.timeBase.plusMinutes(125);
        String deliveryLocationID = this.locationService.getLocationByName("Templiers").orElseThrow(
                () -> new IllegalArgumentException("Location not found")
        ).getId();
        GroupOrderStatus status = GroupOrderStatus.valueOf(groupOrderStatus.toUpperCase());
        String groupID = this.facade.createGroupOrder(this.userID1, deliveryLocationID, null);
        this.groupOrder = this.facade.getGroupOrderById(groupID);

        if (status == GroupOrderStatus.INITIALIZED) {
            this.singleOrder = this.facade.getOrderById(this.groupOrder.getSubOrderIDs().getFirst());
        } else if (status == GroupOrderStatus.COMPLETING) {
            this.singleOrder = placeOrder(this.userID1, this.restaurant.getMenu().getFirst());
        } else if (status == GroupOrderStatus.FINALISING) {
            this.facade.joinGroupOrder(this.userID2, this.groupOrder.getGroupID());
            this.singleOrder2 = placeOrder(this.userID2, this.restaurant.getMenu().getLast());
            this.singleOrder = placeOrder(this.userID1, this.restaurant.getMenu().getFirst());
            this.facade.payOrder(singleOrder2.getId());
            this.facade.validateGroupOrder(this.singleOrder.getId(), deliveryTime);
        } else if (status == GroupOrderStatus.IN_PREPARATION) {
            this.singleOrder = placeOrder(this.userID1, this.restaurant.getMenu().getFirst());
            this.facade.payOrder(this.singleOrder.getId());
            this.facade.validateGroupOrder(this.singleOrder.getId(), deliveryTime);
            this.facade.confirmGroupOrder(this.singleOrder.getId());
        }

        assertEquals(this.groupOrder.getStatus(), GroupOrderStatus.valueOf(groupOrderStatus.toUpperCase()));
    }

    @And("in a restaurant with a {int}% on {int} percentage discount")
    public void inARestaurantWithARatioPercentageDiscount(int ratio, int content) {
        this.ratio = ratio;
        this.restaurant.addDiscountStrategy(new PercentageDiscount(ratio, content));
    }

    @And("no delivery time provided in the group order")
    public void noDeliveryTimeProvidedInTheGroupOrder() {
        assertNull(this.groupOrder.getDeliveryTime());
    }

    @And("an order completed within the group")
    public void anOrderCompletedWithinTheGroup() {
        assertTrue(this.groupOrder.getSubOrderIDs().contains(singleOrder.getId()));
        assertEquals(OrderStatus.COMPLETED, this.facade.getOrderById(singleOrder.getId()).getStatus());
    }

    @And("a set of {int} menu items in the group order")
    public void MenuItemsInTheGroupOrder(int content) {
        assertEquals(content, this.groupOrder.getSubOrderIDs().stream().mapToInt(
                subOrderID -> this.facade.getOrderById(subOrderID).getOrderItems().stream().mapToInt(OrderItem::getQuantity).sum()
        ).sum());
    }

    @And("a minimum delay of {int} minute\\(s) before restaurant availability for order contents")
    public void aMinimumDelayOfDelayMinutesBeforeRestaurantAvailabilityForOrderContents(int delay) throws MenuItemNotFoundException {
        int availabilityDelay;
        do {
            delayRestautantAvailability();
            availabilityDelay = ((int) this.timeBase.toLocalDate().atTime(0, 0, 0).until(
                    ((OrderCoordinator) this.facade.getOrderCoordinator()).getClosestGroupDeliveryTime(this.groupOrder.getSubOrderIDs(), this.timeBase),
                    MINUTES) / 10) * 10 + 10;
            Logger.getGlobal().info("Availability delay: " + availabilityDelay
                    + " minutes, expected delay: " + delay + " minutes");
        } while (availabilityDelay < delay);
    }

    @When("the campus user provides a delivery time {int} minute\\(s) from opening time")
    public void theCampusUserProvidesADeliveryTimeMinutesFromOpeningTime(int duration) {
        this.deliveryTime = this.timeBase.plusMinutes(duration);
    }

    @When("the campus user validates the group order")
    public void theCampusUserValidatesTheGroupOrder() {
        try {
            this.facade.validateGroupOrder(this.singleOrder.getId(), this.deliveryTime);
        } catch (IllegalStateException | IllegalArgumentException exception) {
            this.exception = exception;
        }
    }

    @When("the campus user confirms the group order")
    public void theCampusUserConfirmsTheGroupOrder() {
        try {
            this.facade.payOrder(this.singleOrder.getId());
            this.facade.confirmGroupOrder(this.singleOrder.getId());
        } catch (IllegalStateException exception) {
            this.exception = exception;
        }
    }

    @And("a percentage discount is applied to each sub-order in the group order")
    public void aDiscountOfPercentIsAppliedToEachSubOrderInTheGroupOrder() {
        assertEquals(this.facade.getUser(this.userID1), this.singleOrder.getUser());
        assertEquals(this.facade.getUser(this.userID2), this.singleOrder2.getUser());
        assertEquals(this.singleOrder.getTotalAmount() * ((double) this.ratio / 100), this.singleOrder.getUser().getBalance(), 0.01);
        assertEquals(this.singleOrder2.getTotalAmount() * ((double) this.ratio / 100), this.singleOrder2.getUser().getBalance(), 0.01);
    }

    @Then("a group validating error message with minimum delay is displayed")
    public void anOrderValidatingErrorMessageWithMinimumDelayIsDisplayed() {
        assertTrue(this.exception.getMessage().matches("Expected delivery time must be at least \\d+ hour\\(s\\) and \\d+ minute\\(s\\) from now"));
    }

    @Then("the group validating error message {string} is displayed")
    public void theOrderValidatingErrorMessageIsDisplayed(String exception) {
        assertEquals(exception, this.exception.getMessage());
    }

    @Then("the group completing error message {string} is displayed")
    public void theOrderCompletingErrorMessageIsDisplayed(String exception) {
        assertEquals(exception, this.exception.getMessage());
    }

    @Then("the confirmation moment is recorded")
    public void theConfirmationMomentIsRecorded() {
        assertNotNull(this.groupOrder.getConfirmationMoment());
    }

    @And("the closing group order status is {string}")
    public void theGroupOrderStatusIs(String groupOrderStatus) {
        assertEquals(GroupOrderStatus.valueOf(groupOrderStatus.toUpperCase()), this.groupOrder.getStatus());
    }

}