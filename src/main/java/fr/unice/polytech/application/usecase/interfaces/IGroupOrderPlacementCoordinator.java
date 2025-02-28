package fr.unice.polytech.application.usecase.interfaces;

import fr.unice.polytech.application.exceptions.EntityNotFoundException;
import fr.unice.polytech.domain.models.delivery.DeliveryLocation;
import fr.unice.polytech.domain.models.groupOrder.GroupOrder;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;

import java.time.LocalDateTime;
import java.util.List;

public interface IGroupOrderPlacementCoordinator {
    GroupOrder createGroupOrder(String deliveryLocationName);
    GroupOrder createGroupOrder(String deliveryLocationName, LocalDateTime deliveryTime);
    void joinGroupOrder(String groupOrderId, String userId, String restaurantName, LocalDateTime orderTime);
    List<MenuItem> getAvailableMenuItems(String groupOrderId, Restaurant restaurant) ;
    void addSubOrderToGroupOrder(String groupOrderId, Order subOrder);
    Order createSubOrder(String groupOrderId, String userId, String restaurantId, LocalDateTime orderTime);








    }
