package fr.unice.polytech;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import fr.unice.polytech.domain.models.delivery.DeliveryLocation;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.payment.PaymentDetails;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import fr.unice.polytech.domain.models.user.User;

public class Demo {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger(Demo.class.getName());
        Scanner scanner = new Scanner(System.in);

        // Precondition: User has an account and is logged in
        // Look at the UserRepository to get 2 dummy users
        final String USER_ID = "1";

        // Start order placement
        Facade facade = new Facade();

        LocalDateTime now = LocalDateTime.parse("2024-10-14T13:00:00");
        // Display available restaurants
        List<Restaurant> restaurants = facade.getOrderCoordinator().browseRestaurants(now);
        logger.info("Available restaurants:");
        for (Restaurant restaurant : restaurants) {
            logger.info(restaurant.getId() + ": " + restaurant.getName());
        }

        // Select a restaurant
        logger.info("Enter restaurant ID: ");
        String restaurantId = scanner.nextLine();

        // Display delivery locations
        List<DeliveryLocation> locations = facade.getDeliveryLocations();
        logger.info("Available delivery locations:");
        for (DeliveryLocation location : locations) {
            logger.info(location.getId() + ": " + location.getName());
        }

        // Select delivery location
        logger.info("Enter delivery location ID: ");
        String locationId = scanner.nextLine();

        // Display the restaurant's opening hours (schedule)
        logger.info("Restaurant is open:");
        List<Schedule> schedules = facade.getRestaurantSchedule(restaurantId);
        for (Schedule schedule : schedules) {
            logger.info(schedule.getDay() + ": " + schedule.getStartTime() + " - " + schedule.getEndTime() + " (" + schedule.getNumberOfWorkingStaff() + " staffs)");
        }

        // Select a time for delivery
        logger.info("Please select a day and time for delivery:");
        logger.info("Enter day (e.g. Monday): ");
        String day = scanner.nextLine();
        logger.info("Enter time (e.g. 12:00): ");
        String time = scanner.nextLine();
        LocalDate date = DayOfWeek.valueOf(day.toUpperCase()) == DayOfWeek.MONDAY ? LocalDate.of(2024, 10, 14) : LocalDate.of(2024, 10, 15);
        LocalTime localTime = LocalTime.parse(time);
        LocalDateTime deliveryTime = LocalDateTime.of(date, localTime);

        // Create a new order using orderFacade
        Order order = facade.getOrderCoordinator().createOrder(restaurantId, USER_ID, locationId, deliveryTime);

        while (true) {
            try {
                // Display menu items
                List<MenuItem> menuItems = facade.getOrderCoordinator().getAvailableMenuItems(restaurantId, order.getDeliveryTime());
                logger.info("Menu items for :");
                for (MenuItem item : menuItems) {
                    logger.info(item.getId() + ": " + item.getName() + " - $" + item.getPrice());
                }
                // Add items to cart
                logger.info("Enter item ID to add to cart (or 'done' to finish): ");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("done")) break;

                logger.info("Enter quantity: ");
                int quantity = Integer.parseInt(scanner.nextLine());

                MenuItem selectedItem = menuItems.stream()
                        .filter(item -> item.getId().equals(input))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Invalid item ID"));

                facade.getOrderCoordinator().addMenuItemToOrder(USER_ID, selectedItem, quantity);
                logger.info("Delivery time: " + order.getDeliveryTime());
            } catch (Exception e) {
                logger.info("Error: " + e.getMessage());
            }
        }

        // Process payment
        User user = order.getUser();
        logger.info("Payment methods available:");
        for (int i = 0; i < user.getPaymentDetails().size(); i++) {
            PaymentDetails details = user.getPaymentDetails().get(i);
            int finalI = i;
            logger.info(() -> (finalI + 1) + ": Card - " + details.getCardNumber());
        }
        logger.info("Select your preferred method: ");
        String paymentSelected = scanner.nextLine();
        PaymentDetails selectedPaymentDetails = user.getPaymentDetails().get(Integer.parseInt(paymentSelected) - 1);

        boolean success = facade.getOrderCoordinator().processPayment(order);
        logger.info(() -> "Payment result: " + (success ? "Success" : "Failure"));

        // Place order
        facade.getOrderCoordinator().placeOrder(order);
        logger.info("Order placed with ID: " + order.getId());
    }
}
