package fr.unice.polytech.application.dto;

import fr.unice.polytech.domain.models.delivery.DeliveryLocation;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.order.OrderItem;
import fr.unice.polytech.domain.models.order.OrderStatus;
import fr.unice.polytech.domain.models.restaurant.MenuItem;

import fr.unice.polytech.domain.models.user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class OrderDTO {
    private String id;
    private User user;
    private List<OrderItem> orderItems;
    private DeliveryLocation deliveryLocation;
    private String deliveryTime;
    private String orderTime;
    private double totalAmount;
    private RestaurantDTO restaurantDTO;
    private OrderStatus status;
    private boolean isSubOrder;

    public OrderDTO(Order order) {
        this.id = order.getId() != null ? order.getId() : "";
        this.user = order.getUser() != null ? order.getUser() : new User();  // Assuming User has a default constructor
        this.orderItems = order.getOrderItems() != null ? order.getOrderItems() : Collections.emptyList();
        this.deliveryLocation = order.getDeliveryLocation() != null ? order.getDeliveryLocation() : new DeliveryLocation();  // Assuming DeliveryLocation has a default constructor
        this.deliveryTime = order.getDeliveryTime() != null ? order.getDeliveryTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "0000-01-01T00:00:00";
        this.orderTime = order.getOrderTime() != null ? order.getOrderTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "0000-01-01T00:00:00";
        this.totalAmount = order.getTotalAmount() >= 0 ? order.getTotalAmount() : 0;
        this.restaurantDTO = order.getRestaurant() != null ? new RestaurantDTO(order.getRestaurant()) : null;  // Convert Restaurant to RestaurantDTO
        this.status = order.getStatus();
        this.isSubOrder = order.isSubOrder();
    }

    // Empty constructor for initialization without parameters
    OrderDTO() {
    }

    // Getters
    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public DeliveryLocation getDeliveryLocation() {
        return deliveryLocation;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public RestaurantDTO getRestaurantDTO() {
        return restaurantDTO;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public boolean isSubOrder() {
        return isSubOrder;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public void setDeliveryLocation(DeliveryLocation deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setRestaurantDTO(RestaurantDTO restaurantDTO) {
        this.restaurantDTO = restaurantDTO;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setAsSubOrder(boolean isSubOrder) {
        this.isSubOrder = isSubOrder;
    }
}
