package fr.unice.polytech.application.usecase;

import fr.unice.polytech.application.port.ILocationRepository;
import fr.unice.polytech.application.port.IPaymentService;
import fr.unice.polytech.application.usecase.interfaces.ILocationService;
import fr.unice.polytech.application.usecase.interfaces.IOrderPlacementCoordinator;
import fr.unice.polytech.application.usecase.interfaces.IOrderService;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantCapacityService;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantService;
import fr.unice.polytech.application.usecase.interfaces.IUserService;
import fr.unice.polytech.domain.exceptions.MenuItemNotFoundException;
import fr.unice.polytech.domain.models.delivery.DeliveryLocation;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.order.OrderItem;
import fr.unice.polytech.domain.models.order.OrderStatus;
import fr.unice.polytech.domain.models.payment.PaymentResult;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.user.User;
import fr.unice.polytech.infrastructure.external.PaymentService;
import fr.unice.polytech.infrastructure.repository.inmemory.*;


import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

public class OrderCoordinator implements IOrderPlacementCoordinator {
    private final IUserService userService;
    private final IRestaurantService restaurantService;
    private final IRestaurantCapacityService restaurantCapacityService;
    private final IOrderService orderService;
    private final IPaymentService paymentService;
    private final ILocationService locationService;

    public OrderCoordinator(IUserService userService,
                            IRestaurantService restaurantService,
                            IRestaurantCapacityService restaurantCapacityService,
                            IOrderService orderService,
                            IPaymentService paymentService,
                            ILocationService locationService) {
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.restaurantCapacityService = restaurantCapacityService;
        this.orderService = orderService;
        this.paymentService = paymentService;
        ILocationRepository locationRepository = new LocationRepository();
        this.locationService = locationService;
    }

    public OrderCoordinator() {
        this(
                new UserService(new UserRepository()),
                new RestaurantService(new RestaurantRepository()),
                new RestaurantCapacityService(),
                new OrderService(new OrderRepository()),
                new PaymentService(),
                new LocationService(new LocationRepository())
        );
    }

    @Override
    public List<Restaurant> browseRestaurants(LocalDateTime orderTime) {
        return restaurantService.getAvailableRestaurants(orderTime);
    }

    @Override
    public Order chooseRestaurant(String userId, String restaurantId) {
        Optional<Order> userCart = getUserCart(userId);
        if (userCart.isEmpty()) {
            throw new IllegalStateException("A subOrder should have been created before");
        }
        Order order = userCart.get();

        if (!order.isSubOrder()) {
            throw new IllegalStateException("Individual orders restaurants are chosen at creation");
        } else {
            Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
            checkDeliveryTime(order.getDeliveryTime(), restaurant);
            order.setRestaurant(restaurant);
            order.clearOrderItems();
            order.setAsSubOrder(true);
        }
        return order;
    }

    @Override
    public Order createOrder(String restaurantId, String userId, String deliveryLocationId, LocalDateTime deliveryDate) {
        User user = userService.getUserById(userId).orElseThrow(
                () -> new IllegalStateException("Unregistered cannot place an order")
        );
        DeliveryLocation location = locationService.getLocationById(deliveryLocationId).orElseThrow(
                () -> new IllegalStateException("Invalid delivery location")
        );

        Restaurant restaurant = null;

        if (restaurantId != null) {
            restaurant = restaurantService.getRestaurantById(restaurantId);
           // checkDeliveryTime(deliveryDate, restaurant);
        }


        clearUserCart(userId);

        return orderService.createOrder(restaurant, user, location, deliveryDate);

    }

    private void checkDeliveryTime(LocalDateTime deliveryDate, Restaurant restaurant) {
        if (deliveryDate != null) {
            // TODO: Uncomment for production
            // if (!Utils.isInFuture(deliveryDate)) {
            //    throw new IllegalStateException("Delivery date cannot be in the past");
            //}
            boolean isDeliveryPossible = restaurantCapacityService.isDeliveryPossible(restaurant, deliveryDate);
            // FIXME: orderTime for testing purposes only
            //  Order time will be the time of order creation in production and should not be passed as a parameter
                /*boolean isOrderTimeValid = restaurantCapacityService.isOrderTimeValid(restaurant, orderTime);
                if (!isOrderTimeValid) {
                    throw new IllegalStateException("Order time is not valid");
                }*/
            if (!isDeliveryPossible) {
                throw new IllegalStateException("Delivery is not possible at the specified location and time"
                        + restaurantCapacityService.getEarliestPossibleDeliveryDate(restaurant, new ArrayList<>(), deliveryDate));
            }
        }
    }

    @Override
    public LocalDateTime validateSubOrders(List<String> subOrderIDs, LocalDateTime deliveryTime) {
        for (String orderID : subOrderIDs) {
            Order subOrder = getOrderById(orderID);
            if (subOrder.getStatus() != OrderStatus.COMPLETED && subOrder.getStatus() != OrderStatus.CONFIRMED) {
                throw new IllegalStateException("Sub-order is not placed");
            }
        }
        return getClosestGroupDeliveryTime(subOrderIDs, deliveryTime);
    }

    public LocalDateTime getClosestGroupDeliveryTime(List<String> subOrderIDs, LocalDateTime targetedTime) {
        LocalDateTime earliestDeliveryTime = null;

        Map<Restaurant, List<String>> subOrdersByRestaurant = subOrderIDs.stream()
                .collect(
                        HashMap::new,
                        (map, orderID) -> {
                            Order subOrder = getOrderById(orderID);
                            map.computeIfAbsent(subOrder.getRestaurant(), k -> new ArrayList<>());
                            map.computeIfPresent(subOrder.getRestaurant(), (k, v) -> {
                                v.add(orderID);
                                return v;
                            });
                        },
                        Map::putAll
                );

        for (Map.Entry<Restaurant, List<String>> entry : subOrdersByRestaurant.entrySet()) {
            Restaurant temp = entry.getKey().getCopy();

            for (String orderID : entry.getValue()) {
                Order subOrder = getOrderById(orderID);

                Optional<LocalDateTime> potentialDeliveryTime = restaurantCapacityService.getEarliestPossibleDeliveryDate(
                        temp, subOrder.getOrderItems(), targetedTime
                );
                restaurantCapacityService.reduceCapacity(
                        temp, subOrder.getOrderItems(), targetedTime
                );

                if (potentialDeliveryTime.isEmpty()) {
                    throw new IllegalStateException("Cannot calculate the earliest delivery time");
                }

                if (earliestDeliveryTime == null || potentialDeliveryTime.get().isAfter(earliestDeliveryTime)) {
                    earliestDeliveryTime = potentialDeliveryTime.get();
                }
            }
        }

        return earliestDeliveryTime;
    }

    @Override
    public void updateSubOrdersDeliveryTime(List<String> subOrderIDs, LocalDateTime deliveryTime) {
        if (deliveryTime != null) {
            for (String orderID : subOrderIDs) {
                Order subOrder = getOrderById(orderID);
                subOrder.setDeliveryTime(deliveryTime, true);
                restaurantCapacityService.reduceCapacity(
                        subOrder.getRestaurant(), subOrder.getOrderItems(), subOrder.getDeliveryTime()
                );
            }
        }
    }

    @Override
    public void confirmSubOrders(List<String> subOrderIDs) {
        for (String orderID : subOrderIDs) {
            if (getOrderById(orderID).getStatus() != OrderStatus.CONFIRMED) {
                throw new IllegalStateException("Sub-order's bill not paid");
            }
        }
    }

    @Override
    public void applySubOrdersDiscount(List<String> subOrderIDs, int totalQuantity) {
        for (String orderID : subOrderIDs) {
            Order subOrder = getOrderById(orderID);
            subOrder.getRestaurant().applyGroupPercentageDiscount(subOrder, totalQuantity);
        }
    }

    @Override
    public List<MenuItem> getAllMenuItems(String restaurantId) {
        return restaurantService.getRestaurantById(restaurantId).getMenu();
    }

    @Override
    public Optional<Order> getUserCart(String userId) {
        return orderService.getAllOrderByUserId(userId).stream()
                .filter(order -> order.getStatus() == OrderStatus.PENDING)
                .max(Comparator.comparing(Order::getOrderTime));
    }

    @Override
    public void clearUserCart(String userId) {
        Optional<Order> userCart = getUserCart(userId);
        if (userCart.isPresent()) {
            userCart.get().setAsSubOrder(false);
            userCart.get().setTotalAmount(0.0);
            userCart.get().setDeliveryTime(null, false);
            userCart.get().setDeliveryLocation(null, false);
            userCart.get().setRestaurant(null);
            userCart.get().clearOrderItems();

            userCart.get().setStatus(OrderStatus.CANCELLED);
        }
    }

    @Override
    public List<LocalDateTime> getAvailableDeliveryTime(String restaurantId, LocalDateTime orderTime) {
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
        return restaurantCapacityService.getAllAvailableDeliveryTime(restaurant, orderTime);
    }


    @Override
    public List<MenuItem> getAvailableMenuItems(String restaurantId, LocalDateTime deliveryDate) {
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
        List<MenuItem> availableMenuItems = restaurantCapacityService.getAvailableMenuItems(restaurant, deliveryDate);
        if (availableMenuItems.isEmpty()) {
            throw new IllegalStateException("No menu items available for the specified delivery time");
        }
        return availableMenuItems;
    }

    @Override
    public Order addMenuItemToOrder(String userId, MenuItem selectedItem, int quantity) {
        userService.getUserById(userId).orElseThrow(
                () -> new IllegalStateException("Please register before placing an order")
        );
        // Retrieve the order using the user ID
        Order order = getUserCart(userId).orElseThrow(
                () -> new IllegalStateException("Please create an order before adding items to the cart")
        );
        Restaurant restaurant = order.getRestaurant();
        if (restaurant == null) {
            throw new IllegalStateException("Please choose a restaurant before adding items to the cart");
        }

        // Menu item validation
        Optional<MenuItem> menuItemFromRestaurant = restaurant.getMenu().stream()
                .filter(item -> item.equals(selectedItem))
                .findFirst();
        if (menuItemFromRestaurant.isEmpty()) {
            throw new IllegalStateException("This selected menu item is not available in the restaurant");
        }

        OrderItem newOrderItem = new OrderItem(selectedItem, quantity);
        order.getOrderItems().stream()
                .filter(item -> item.getItem().equals(selectedItem))
                .findFirst()
                .ifPresent(item -> newOrderItem.setQuantity(item.getQuantity() + quantity));

        // Adjust the delivery time according to the preparation time
        LocalDateTime deliveryDateTime = order.getDeliveryTime();
        if (deliveryDateTime != null) {
            LocalDateTime earliestDeliveryTime = restaurantCapacityService.getEarliestPossibleDeliveryDate(restaurant, List.of(newOrderItem), deliveryDateTime)
                    .orElseThrow(() -> new IllegalStateException("Cannot calculate the earliest delivery time"));
            LocalDateTime earliestDeliveryDateTime = deliveryDateTime.isBefore(earliestDeliveryTime) ? earliestDeliveryTime : deliveryDateTime;
            if (!order.isSubOrder()) order.setDeliveryTime(earliestDeliveryDateTime, false);
        }

        System.out.println("Adding menu item to order");
        // Update the order with the selected menu item
        orderService.addItemToCart(order.getId(), newOrderItem);

        return order;
    }

    @Override
    public Order addItemToOrder(String orderId, String menuItemId, int quantity){
        Order order = orderService.getOrderById(orderId);
        userService.getUserById(order.getUser().getId()).orElseThrow(
                () -> new IllegalStateException("Please register before placing an order")
        );

        Restaurant restaurant = order.getRestaurant();

        // Menu item validation
        MenuItem selectedItem;
        try {
            selectedItem =  restaurantService.getRestaurantById(restaurant.getId()).getMenuItemById(menuItemId);
        } catch (MenuItemNotFoundException e) {
            throw new IllegalStateException("Selected menu item is not available in the restaurant");
        }

        Optional<MenuItem> menuItemFromRestaurant = restaurant.getMenu().stream()
                .filter(item -> item.equals(selectedItem))
                .findFirst();
        if (menuItemFromRestaurant.isEmpty()) {
            throw new IllegalStateException("Selected menu item is not available in the restaurant");
        }

        OrderItem newOrderItem = new OrderItem(selectedItem, quantity);
        order.getOrderItems().stream()
                .filter(item -> item.getItem().equals(selectedItem))
                .findFirst()
                .ifPresent(item -> newOrderItem.setQuantity(item.getQuantity() + quantity));

        // Adjust the delivery time according to the preparation time
        LocalDateTime deliveryDateTime = order.getDeliveryTime();
        if (deliveryDateTime != null) {
            LocalDateTime earliestDeliveryTime = restaurantCapacityService.getEarliestPossibleDeliveryDate(restaurant, List.of(newOrderItem), deliveryDateTime)
                    .orElseThrow(() -> new IllegalStateException("Cannot calculate the earliest delivery time"));
            LocalDateTime earliestDeliveryDateTime = deliveryDateTime.isBefore(earliestDeliveryTime) ? earliestDeliveryTime : deliveryDateTime;
            if (!order.isSubOrder()) order.setDeliveryTime(earliestDeliveryDateTime, false);
        }

        // Update the order with the selected menu item
        orderService.addItemToCart(order.getId(), newOrderItem);

        return order;
    }


    @Override
    public boolean validatelOrder(Order order) {
        if (order.isSubOrder()) {
            return orderService.validateGroupOrder(order);
        }
        return orderService.validateIndividualOrder(order);
    }

    @Override
    public boolean placeOrder(Order order) throws IllegalStateException {
        validatelOrder(order);
        // Check capacity and place the order
        Restaurant restaurant = order.getRestaurant();
        if (restaurant == null) {
            throw new IllegalStateException("Order restaurant not chosen");
        }

        if (order.getDeliveryTime() != null && !restaurantCapacityService.canAcceptOrder(restaurant, order.getOrderItems(), order.getDeliveryTime())) {
            Logger.getGlobal().warning("Not enough capacity");
            return false;
        }


        order.setStatus(OrderStatus.COMPLETED);
        // Place the order

        return orderService.placeOrder(order);
    }

    @Override
    public boolean processPayment(Order order) {
        PaymentResult result = paymentService.processPayment(order, order.getUser().getPaymentDetails().getFirst());
        if (!result.success()) {
            throw new IllegalStateException("Payment failed: " + result.message());
        }
        order.setStatus(OrderStatus.CONFIRMED);
        if (!order.isSubOrder()) {
            order.getRestaurant().applyDiscountStrategy(order);
            userService.updateUser(order.getUser());
        }

        Restaurant restaurant = order.getRestaurant();
        boolean orderPlaced = orderService.placeOrder(order);
        if (orderPlaced) {
            // Reduce restaurant capacity
            restaurantCapacityService.reduceCapacity(restaurant, order.getOrderItems(), order.getDeliveryTime());

        }
        return orderPlaced;
    }

    @Override
    public boolean applyDiscount(Order order) {
        //I don't get what has to be done here
        return false;
    }

    @Override
    public Order getOrderById(String orderId){
        return orderService.getOrderById(orderId);
    }

}
