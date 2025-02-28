package fr.unice.polytech.application.usecase;

import java.time.LocalDateTime;
import java.util.List;

import fr.unice.polytech.application.exceptions.EntityNotFoundException;
import fr.unice.polytech.application.port.IOrderRepository;
import fr.unice.polytech.application.usecase.interfaces.IOrderService;
import fr.unice.polytech.domain.models.delivery.DeliveryLocation;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.order.OrderItem;
import fr.unice.polytech.domain.models.order.OrderStatus;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.user.User;
import fr.unice.polytech.utils.Utils;

public class OrderService implements IOrderService {
    private final IOrderRepository orderRepository;

    public OrderService(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId).orElseThrow(
            () -> new EntityNotFoundException("Order", orderId)
        );
    }


    @Override
    public Order addItemToCart(String orderId, OrderItem orderItem) {
        Order order = null;
        try {
            order = getOrderById(orderId);
            if (order != null) {
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace(); // Affiche la trace d'erreur complète dans la console
            return null; // Fin prématurée si une erreur se produit
        }

        try {
            order.addOrderItem(orderItem);
        } catch (Exception e) {
            e.printStackTrace(); // Affiche la trace d'erreur complète dans la console
            return null; // Fin prématurée si une erreur se produit
        }

        try {
            orderRepository.update(order);
        } catch (Exception e) {
            e.printStackTrace(); // Affiche la trace d'erreur complète dans la console
            return null; // Fin prématurée si une erreur se produit
        }

        // Fin de la méthode

        return order;
    }


    @Override
    public Order createOrder(Restaurant restaurant, User user, DeliveryLocation deliveryLocation, LocalDateTime deliveryTime) {
        String orderId = Utils.generateUniqueId();
        Order.OrderBuilder orderBuilder = new Order.OrderBuilder(orderId, user, restaurant, deliveryLocation, deliveryTime, LocalDateTime.now()).setStatus(OrderStatus.PENDING);
        if (restaurant == null) {
            orderBuilder.setAsSubOrder("");
        }
        Order order = orderBuilder.build();
        orderRepository.add(order);
        return order;
    }

    @Override
    public boolean placeOrder(Order order) {
        orderRepository.update(order);
        return orderRepository.findById(order.getId()).isPresent();
    }

    @Override
    public boolean validateGroupOrder(Order order) {
        if (order.getOrderItems().isEmpty()) {
            return false;
        }
        if (order.isSubOrder()) {
            return order.getDeliveryTime() != null && order.getDeliveryLocation() != null;
        }
        return false;
    }

    @Override
    public boolean validateIndividualOrder(Order order) {
        if (order.getOrderItems().isEmpty()) {
            return false;
        }
        if (!order.isSubOrder()) {
            return order.getDeliveryTime() != null && order.getDeliveryLocation() != null;
        }
        return false;
    }

    @Override
    public List<Order> getAllOrderByUserId(String userId) {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .filter(order -> order.getUser().getId().equals(userId))
                .toList();
    }


}
