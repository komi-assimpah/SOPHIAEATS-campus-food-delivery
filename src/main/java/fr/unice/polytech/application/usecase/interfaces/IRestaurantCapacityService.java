package fr.unice.polytech.application.usecase.interfaces;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import fr.unice.polytech.domain.models.order.OrderItem;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;

/**
 * RestaurantCapacityService is responsible for managing the restaurant's capacity,
 * ensuring that the restaurant cannot exceed its available resources (e.g., staff, preparation capabilities).
 * Its domain logic is central to the system's behavior in controlling overload and capacity management
 */
public interface IRestaurantCapacityService {

    boolean isDeliveryPossible(Restaurant restaurant, LocalDateTime deliveryDate);

    List<MenuItem> getAvailableMenuItems(Restaurant restaurant, LocalDateTime deliveryDate);

    boolean canAcceptOrder(Restaurant restaurant, List<OrderItem> orderItems, LocalDateTime deliveryDate);

    void reduceCapacity(Restaurant restaurant, List<OrderItem> orderItems, LocalDateTime deliveryDate);


    List<LocalDateTime> getAvailableDeliveryTimeForAllRestaurants(List<Restaurant> restaurants, LocalDateTime orderTime);

    Optional<LocalDateTime> getEarliestPossibleDeliveryDate(Restaurant restaurant, List<OrderItem> orderItems, LocalDateTime deliveryDate);


    //boolean isOrderTimeValid(Restaurant restaurant, LocalDateTime orderTime);

    List<LocalDateTime> getAvailableDeliveryTime(Restaurant restaurant, LocalDateTime orderTime);

    boolean isOrderTimeValid(Restaurant restaurant, LocalDateTime orderTime);

    List<LocalDateTime> getAllAvailableDeliveryTime(Restaurant restaurant, LocalDateTime orderTime);
}
