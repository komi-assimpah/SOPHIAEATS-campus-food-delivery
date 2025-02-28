package fr.unice.polytech;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import fr.unice.polytech.application.exceptions.EntityNotFoundException;
import fr.unice.polytech.application.exceptions.RestaurantNotFoundException;
import fr.unice.polytech.application.port.ILocationRepository;
import fr.unice.polytech.application.port.IOrderRepository;
import fr.unice.polytech.application.port.IRestaurantRepository;
import fr.unice.polytech.application.port.IUserRepository;
import fr.unice.polytech.application.usecase.*;
import fr.unice.polytech.application.usecase.interfaces.*;
import fr.unice.polytech.domain.exceptions.MenuItemNotFoundException;
import fr.unice.polytech.domain.models.delivery.DeliveryLocation;
import fr.unice.polytech.domain.models.groupOrder.GroupOrder;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.order.OrderItem;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import fr.unice.polytech.domain.models.restaurant.discountstrategy.DiscountStrategy;
import fr.unice.polytech.domain.models.user.User;
import fr.unice.polytech.infrastructure.external.PaymentService;
import fr.unice.polytech.infrastructure.repository.inmemory.*;

public class Facade {

    private final IOrderPlacementCoordinator orderPlacementCoordinator;
    private final IGroupOrderService groupOrderService;
    private final IRestaurantService restaurantService;
    private final ILocationService locationService;
    private final IRestaurantScheduleManager restaurantScheduleManager;
    private final IUserService userService;
    private final IRestaurantCapacityService restaurantCapacityService;

    public Facade() {

        IUserRepository userRepository = new UserRepository();
        IRestaurantRepository restaurantRepository = new RestaurantRepository();
        IOrderRepository orderRepository = new OrderRepository();
        this.restaurantService = new RestaurantService(restaurantRepository);
        ILocationRepository locationRepository = new LocationRepository();
        this.locationService = new LocationService(locationRepository);
        this.orderPlacementCoordinator = new OrderCoordinator(
                new UserService(userRepository),
                restaurantService,
                new RestaurantCapacityService(),
                new OrderService(orderRepository),
                new PaymentService(),
                locationService
        );
        this.groupOrderService = new GroupOrderService(new GroupOrderRepository());
        this.restaurantScheduleManager = new RestaurantScheduleManager(restaurantRepository);
        this.userService = new UserService(userRepository);
        this.restaurantCapacityService = new RestaurantCapacityService();

    }

    public IRestaurantService getRestaurantService(){
        return restaurantService;
    }

    public ILocationService getLocationService(){
        return locationService;
    }

    public IGroupOrderService getGroupOrderService(){
        return groupOrderService;
    }

    public List<DeliveryLocation> getDeliveryLocations() {
        return locationService.getDeliveryLocations();
    }

    public List<Schedule> getRestaurantSchedule(String restaurantId) {
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
        return restaurant.getSchedules();
    }

    public LocalDateTime reserveDeliveryTime(String restaurantId, LocalDateTime deliveryTime) {
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
        for (Schedule schedule : restaurant.getSchedules()) {
            if(schedule.reserveTimeSlot(deliveryTime, LocalDateTime.now())) {
                return deliveryTime;
            }
        }
        return null;
    }

    public IOrderPlacementCoordinator getOrderCoordinator() {
        return orderPlacementCoordinator;
    }


    public Order createOrder(String restaurantId, String userId, String locationId, LocalDateTime deliveryTime) throws EntityNotFoundException {
        groupOrderService.dropSubOrder(
                orderPlacementCoordinator.getUserCart(userId).map(Order::getId).orElse(null)
        );
        return orderPlacementCoordinator.createOrder(restaurantId, userId, locationId, deliveryTime);
    }

    public void addItems(String orderId, String menuItemId, int quantity) throws MenuItemNotFoundException {
        Order order = orderPlacementCoordinator.getOrderById(orderId);
        MenuItem menuItem = restaurantService.getRestaurantById(order.getRestaurant().getId()).getMenuItemById(menuItemId);
        orderPlacementCoordinator.addMenuItemToOrder(order.getUser().getId(), menuItem, quantity);
    }



    public Restaurant browseRestaurant(String restaurantId) {
        return restaurantService.getRestaurantById(restaurantId);
    }

    public Order chooseRestaurant(String userId, String restaurantId) {
        return orderPlacementCoordinator.chooseRestaurant(userId, restaurantId);
    }

    public boolean payOrder(String orderId) {
        return orderPlacementCoordinator.processPayment(
                orderPlacementCoordinator.getOrderById(orderId)
        );
    }



    public Optional<DeliveryLocation> selectDeliveryLocation(String locationName) {
        return locationService.getLocationByName(locationName);
    }

    public void addDiscountStrategy(DiscountStrategy discountStrategy, String restaurantId) {
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
        restaurant.addDiscountStrategy(discountStrategy);
    }

    public Restaurant getRestaurantByName(String restaurantName) {
        return restaurantService.findByName(restaurantName).orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
    }

    public Restaurant getRestaurantById(String restaurantId) {
        return restaurantService.getRestaurantById(restaurantId);
    }

    public List<MenuItem> getAvailableMenuItems(String restaurantId , LocalDateTime deliveryTime) {
        return orderPlacementCoordinator.getAvailableMenuItems(restaurantId , deliveryTime );
    }

    public List<Restaurant> getAvailableRestaurants(LocalDateTime deliveryTime) {
        return restaurantService.getAvailableRestaurants(deliveryTime);
    }

    public List<Restaurant> getRestaurants(LocalDateTime deliveryTime) {
        return restaurantService.getAvailableRestaurants(deliveryTime);
    }

    public User createUser(String name, String email, String password) {
        return userService.createUser(name, email, password);
    }

    public User getUser(String userId) {
        return userService.getUserById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public boolean placeOrder(String orderId) throws IllegalStateException {
        boolean result = orderPlacementCoordinator.placeOrder(orderPlacementCoordinator.getOrderById(orderId));
        groupOrderService.completeOrderGroup(orderId);
        return result;
    }

    public Restaurant createRestaurant(String name, String street, String city, String zipCode, String country) {
        return restaurantService.createRestaurant(name, street, city, zipCode, country);
    }

    public void addSchedule(String id, Schedule schedule) throws RestaurantNotFoundException, EntityNotFoundException {
        restaurantScheduleManager.addSchedule(id, schedule);
    }

    public Optional<Restaurant> findByName(String restaurantName) {
        return restaurantService.findByName(restaurantName);
    }

    public List<LocalDateTime> getAvailableDeliveryTime(String restaurantName, LocalDateTime parse) {
        Restaurant restaurant = restaurantService.findByName(restaurantName).orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        return restaurantCapacityService.getAvailableDeliveryTime(restaurant, parse);
    }

    public Order getOrderById(String orderId) {
        return orderPlacementCoordinator.getOrderById(orderId);
    }

    public GroupOrder getGroupOrderById(String groupOrderId) throws EntityNotFoundException {
        return groupOrderService.findGroupOrderById(groupOrderId);
    }

    public String createGroupOrder(String userID, String locationID, LocalDateTime expectedDeliveryTime) throws IllegalArgumentException, EntityNotFoundException {
        locationService.getLocationById(locationID)
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));

        groupOrderService.dropSubOrder(
                orderPlacementCoordinator.getUserCart(userID).map(Order::getId).orElse(null)
        );

        return groupOrderService.createGroupOrder(
                orderPlacementCoordinator.createOrder(
                        null, userID, locationID, expectedDeliveryTime).getId(),
                locationID, expectedDeliveryTime);
    }

    public void joinGroupOrder(String userID, String groupID) throws EntityNotFoundException, IllegalStateException {
        userService.getUserById(userID)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        groupOrderService.dropSubOrder(
                orderPlacementCoordinator.getUserCart(userID).map(Order::getId).orElse(null)
        );

        groupOrderService.joinGroupOrder(
                orderPlacementCoordinator.createOrder(
                        null, userID,
                        groupOrderService.findGroupOrderById(groupID).getDeliveryLocationID(),
                        groupOrderService.findGroupOrderById(groupID).getDeliveryTime()
                ).getId(),
                groupID);
    }

    public void validateGroupOrder(String orderID, LocalDateTime expectedDeliveryTime)
            throws IllegalArgumentException, IllegalStateException {
        LocalDateTime closestPossibleDeliveryTime = orderPlacementCoordinator.validateSubOrders(
                groupOrderService.findSubOrderGroup(orderID).getSubOrderIDs(),
                (groupOrderService.findSubOrderGroup(orderID).getDeliveryTime() == null) ?
                        expectedDeliveryTime : groupOrderService.findSubOrderGroup(orderID).getDeliveryTime()
        );
        groupOrderService.validateGroupOrder(orderID, expectedDeliveryTime, closestPossibleDeliveryTime);
        orderPlacementCoordinator.updateSubOrdersDeliveryTime(
                groupOrderService.findSubOrderGroup(orderID).getSubOrderIDs(),
                expectedDeliveryTime
        );
    }

    public void confirmGroupOrder(String orderID) throws IllegalStateException {
        orderPlacementCoordinator.confirmSubOrders(groupOrderService.findSubOrderGroup(orderID).getSubOrderIDs());
        groupOrderService.confirmGroupOrder(orderID);
        orderPlacementCoordinator.applySubOrdersDiscount(
                groupOrderService.findSubOrderGroup(orderID).getSubOrderIDs(),
                groupOrderService.findSubOrderGroup(orderID).getSubOrderIDs().stream().mapToInt(
                        subOrderID -> orderPlacementCoordinator.getOrderById(subOrderID).getOrderItems().stream().mapToInt(OrderItem::getQuantity).sum()
                ).sum()
        );
    }

}