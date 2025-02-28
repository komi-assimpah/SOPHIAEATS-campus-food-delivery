package fr.unice.polytech.application.usecase;

import java.time.LocalDateTime;
import java.util.*;

import fr.unice.polytech.application.usecase.interfaces.IRestaurantCapacityService;
import fr.unice.polytech.domain.models.order.OrderItem;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.Schedule;


public class RestaurantCapacityService implements IRestaurantCapacityService {

    @Override
    public boolean isOrderTimeValid(Restaurant restaurant, LocalDateTime orderTime) {
        return false;
    }

    @Override
    public boolean isDeliveryPossible(Restaurant restaurant, LocalDateTime deliveryDate) {
        return findScheduleForTime(restaurant, deliveryDate).isPresent();
    }

    @Override
    public List<MenuItem> getAvailableMenuItems(Restaurant restaurant, LocalDateTime deliveryDate) {
        // Filter menu items based on restaurant's ability to prepare them in time
        return restaurant.getMenu().stream()
                .filter(menuItem -> canPrepareOnTime(restaurant, menuItem, deliveryDate))
                .toList();
    }

    @Override
    public boolean canAcceptOrder(Restaurant restaurant, List<OrderItem> orderItems, LocalDateTime deliveryDate) {
        return findScheduleForTime(restaurant, deliveryDate)
                .map(schedule -> schedule.canHandleOrder(orderItems, deliveryDate))
                .orElse(false);
    }

    @Override
    public void reduceCapacity(Restaurant restaurant, List<OrderItem> orderItems, LocalDateTime deliveryDate) {
        LocalDateTime deliveryDateTime = (deliveryDate != null) ? deliveryDate : LocalDateTime.now();
        findScheduleForTime(restaurant, deliveryDateTime)
                .ifPresent(schedule -> schedule.reduceCapacity(orderItems));
    }

    @Override
    public List<LocalDateTime> getAvailableDeliveryTime(Restaurant restaurant, LocalDateTime orderTime) {
        return restaurant.getSchedules().stream()
                .filter(schedule -> schedule.isWithinOpeningHours(orderTime))
                .map(schedule -> schedule.getAvailableDeliveryTime(orderTime))
                .toList();
    }

    @Override
    public List<LocalDateTime> getAllAvailableDeliveryTime(Restaurant restaurant, LocalDateTime orderTime) {
        List<LocalDateTime> availableDeliveryTimes = new ArrayList<>();
        for (Schedule schedule : restaurant.getSchedules()) {
            if (schedule.isWithinOpeningHours(orderTime)) {
                availableDeliveryTimes.addAll(schedule.getAllAvailableDeliveryTime(orderTime));
            }
        }
        return availableDeliveryTimes;
    }

    @Override
    public List<LocalDateTime> getAvailableDeliveryTimeForAllRestaurants(List<Restaurant> restaurants, LocalDateTime orderTime) {
        Set<LocalDateTime> availableDeliveryTimes = new HashSet<>();
        for (Restaurant restaurant : restaurants) {
            availableDeliveryTimes.addAll(getAllAvailableDeliveryTime(restaurant, orderTime));
        }
        return new ArrayList<>(availableDeliveryTimes);
    }

    @Override
    public Optional<LocalDateTime> getEarliestPossibleDeliveryDate(Restaurant restaurant, List<OrderItem> orderItems, LocalDateTime deliveryDate) {
        // Calculate the earliest delivery date based on the restaurant’s capacity and preparation time for each item
        LocalDateTime deliveryDateTime = (deliveryDate != null) ? deliveryDate : LocalDateTime.now();
        return findScheduleForTime(restaurant, deliveryDateTime)
                .filter(schedule -> schedule.canHandleOrder(orderItems, deliveryDateTime))
                .or(() -> restaurant.getSchedules().stream().filter(schedule -> schedule.getDay().compareTo(deliveryDateTime.getDayOfWeek()) > 0).min(Comparator.comparing(Schedule::getDay))
                        .or(() -> restaurant.getSchedules().stream().min(Comparator.comparing(Schedule::getDay))))
                .map(schedule -> deliveryDateTime.with(schedule.calculateEarliestDeliveryTime(orderItems, deliveryDateTime)));
    }

    // Utility methods
    private Optional<Schedule> findScheduleForTime(Restaurant restaurant, LocalDateTime time) {
        return restaurant.getSchedules().stream()
                .filter(schedule -> schedule.isWithinOpeningHours(time))
                .findFirst();
    }

    private boolean canPrepareOnTime(Restaurant restaurant, MenuItem menuItem, LocalDateTime deliveryDate) {
        return findScheduleForTime(restaurant, deliveryDate)
                .map(schedule -> schedule.canHandleOrder(List.of(new OrderItem(menuItem, 1)), deliveryDate))
                .orElse(false);
    }


}


