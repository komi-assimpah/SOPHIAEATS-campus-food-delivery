package fr.unice.polytech.domain.models.order;

import fr.unice.polytech.domain.models.delivery.DeliveryLocation;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.user.User;

import java.sql.SQLOutput;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private final String id;
    private final User user;
    private final List<OrderItem> orderItems ;
    private DeliveryLocation deliveryLocation;
    private LocalDateTime deliveryTime;
    private LocalDateTime orderTime;
    private double totalAmount;
    private Restaurant restaurant;
    private OrderStatus status;
    private boolean isSubOrder = false;

    public Order(){
        this.id = null;
        this.user = null;
        this.orderItems = new ArrayList<>();

    }

    private Order(OrderBuilder orderBuilder) {
        this.id = orderBuilder.orderId;
        this.user = orderBuilder.orderUser;
        this.orderItems = new ArrayList<>(orderBuilder.orderItems);
        this.deliveryLocation = orderBuilder.orderDeliveryLocation;
        this.deliveryTime = orderBuilder.orderDeliveryTime;
        this.totalAmount = calculateTotalAmount();
        this.restaurant = orderBuilder.orderRestaurant;
        this.isSubOrder = orderBuilder.isSubOrder;
        this.orderTime = orderBuilder.orderTime;
        this.status = orderBuilder.status;
    }


    private double calculateTotalAmount() {
        return orderItems.stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
    }

    public String getId() {
        return id;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public User getUser() {
        return user;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public boolean isSubOrder() {
        return isSubOrder;
    }

    public void setAsSubOrder(boolean isSubOrder) {
        this.isSubOrder = isSubOrder;
    }

    public void setTotalAmount(double amount) {
        this.totalAmount = amount;
    }

    public LocalDateTime getDeliveryTime() {
        return deliveryTime;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public DeliveryLocation getDeliveryLocation() {
        return deliveryLocation;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public void setDeliveryTime(LocalDateTime deliveryTime, boolean forSubOrder) {
        if (isSubOrder && (!forSubOrder || this.deliveryTime != null)) {
            throw new IllegalStateException("Sub-order's delivery time to set in group");
        }
        this.deliveryTime = deliveryTime;
    }

    public void setDeliveryLocation(DeliveryLocation deliveryLocation, boolean forSubOrder) {
        if (isSubOrder && (!forSubOrder || this.deliveryLocation != null)) {
            throw new IllegalStateException("Sub-order's group delivery address already set");
        }
        this.deliveryLocation = deliveryLocation;
    }

    public void addOrderItem(MenuItem selectedItem, int quantity) {
        OrderItem orderItem = new OrderItem(selectedItem, quantity);
        this.addOrderItem(orderItem);
    }

    public void addOrderItem(OrderItem orderItem) {
        if (orderItems.isEmpty()) {
            orderItems.add(orderItem);
        } else {
            orderItems.stream()
                    .filter(item -> item.equals(orderItem))
                    .findFirst()
                    .ifPresentOrElse(
                            item -> item.setQuantity(item.getQuantity() + orderItem.getQuantity()),
                            () -> orderItems.add(orderItem)
                    );
        }
        totalAmount = calculateTotalAmount();
    }

    public void removeOrderItem(MenuItem selectedItem) {
        orderItems.removeIf(item -> item.getItem().equals(selectedItem));
        totalAmount = calculateTotalAmount();
    }

    public void clearOrderItems() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot clear order items for an order that is not pending");
        }
        orderItems.clear();
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setDeliveryTime(LocalDateTime earliestDeliveryDateTime) {
        this.deliveryTime = earliestDeliveryDateTime;
    }


    public static class OrderBuilder {
        // required
        private final String orderId;
        private Restaurant orderRestaurant;
        private User orderUser;
        private LocalDateTime orderDeliveryTime;
        private DeliveryLocation orderDeliveryLocation;
        private LocalDateTime orderTime;
        // optional
        private OrderStatus status;
        private boolean isSubOrder;
        private List<OrderItem> orderItems = new ArrayList<>();
        private OrderStatus orderStatus;
        private String groupID;


        public OrderBuilder(String orderId, User user, Restaurant restaurant, DeliveryLocation deliveryLocation, LocalDateTime deliveryTime, LocalDateTime orderTime) {
            this.orderId = orderId;
            this.orderUser = user;
            this.orderRestaurant = restaurant;
            this.orderDeliveryLocation = deliveryLocation;
            this.orderDeliveryTime = deliveryTime;
            this.orderTime = orderTime;
        }

        public OrderBuilder setOrderUser(User orderUser) {
            this.orderUser = orderUser;
            return this;
        }

        public OrderBuilder setOrderDeliveryTime(LocalDateTime orderDeliveryTime) {
            this.orderDeliveryTime = orderDeliveryTime;
            return this;
        }

        public OrderBuilder setOrderDeliveryLocation(DeliveryLocation orderDeliveryLocation) {
            this.orderDeliveryLocation = orderDeliveryLocation;
            return this;
        }

        public OrderBuilder setAsSubOrder(String groupID) {
            this.groupID = groupID;
            this.isSubOrder = true;
            return this;
        }

        public String getGroupID() {
            return groupID;
        }

        public OrderBuilder setStatus(OrderStatus status) {
            this.status = status;
            return this;
        }

        public OrderBuilder setOrderRestaurant(Restaurant orderRestaurant) {
            this.orderRestaurant = orderRestaurant;
            return this;
        }

        public OrderBuilder setOrderItems(List<OrderItem> orderItems) {
            this.orderItems = orderItems;
            return this;
        }

        public Order build() {
            validateOrder();
            return new Order(this);
        }

        private void validateOrder() {
            if (orderUser == null || (!isSubOrder && (orderRestaurant == null || orderDeliveryTime == null)) || orderDeliveryLocation == null) {
                throw new IllegalArgumentException("Order is missing required fields");
            }
            if (status == null) {
                this.status = OrderStatus.PENDING;
            }
        }
    }
}


