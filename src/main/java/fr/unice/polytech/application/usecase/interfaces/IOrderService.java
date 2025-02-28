package fr.unice.polytech.application.usecase.interfaces;

import java.time.LocalDateTime;
import java.util.List;

import fr.unice.polytech.domain.models.delivery.DeliveryLocation;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.order.OrderItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.user.User;
import fr.unice.polytech.infrastructure.repository.exceptions.EntityAlreadyExistsException;

/**
 * OrderService will handle the process of placing and managing orders,
 * ensuring that an order can only be placed if the restaurant has the capacity to fulfill it.
 * It coordinates between the Restaurant and Menu entities.
 */
public interface IOrderService {
    Order addItemToCart(String orderId, OrderItem orderItem);


    Order createOrder(Restaurant restaurant, User user, DeliveryLocation deliveryLocation, LocalDateTime deliveryTime) throws EntityAlreadyExistsException;

    Order getOrderById(String orderId);

    List<Order> getAllOrderByUserId(String userId);


    boolean placeOrder(Order order);

    boolean validateGroupOrder(Order order);

    boolean validateIndividualOrder(Order order);
}
