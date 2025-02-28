package fr.unice.polytech.application.usecase.interfaces;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;

public interface IOrderPlacementCoordinator {

    List<Restaurant> browseRestaurants(LocalDateTime orderTime);

    Order createOrder(String restaurantId, String userId, String deliveryLocationId, LocalDateTime deliveryDate);

    List<LocalDateTime> getAvailableDeliveryTime(String restaurantId, LocalDateTime orderTime);

    List<MenuItem> getAvailableMenuItems(String restaurantId, LocalDateTime deliveryDate);

    Order addMenuItemToOrder(String userId, MenuItem selectedItem, int quantity);

    boolean placeOrder(Order order);

    boolean processPayment(Order order);

    //boolean applyDiscount(Order order);

    Order addItemToOrder(String orderId, String menuItemId, int quantity);


    boolean validatelOrder(Order order);

    boolean applyDiscount(Order order);

    Order getOrderById(String orderId);


    Optional<Order> getUserCart(String userId);

    void clearUserCart(String userId);

    Order chooseRestaurant(String userId, String restaurantId);

    LocalDateTime validateSubOrders(List<String> orderIDs, LocalDateTime deliveryTime);

    void updateSubOrdersDeliveryTime(List<String> orderIDs, LocalDateTime deliveryTime);

    void confirmSubOrders(List<String> orderIDs);

    void applySubOrdersDiscount(List<String> orderIDs, int totalQuantity);

    List<MenuItem> getAllMenuItems(String restaurantId);
}
