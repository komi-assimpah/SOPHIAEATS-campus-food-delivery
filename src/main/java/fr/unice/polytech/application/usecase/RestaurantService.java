package fr.unice.polytech.application.usecase;

import fr.unice.polytech.application.exceptions.EntityNotFoundException;
import fr.unice.polytech.application.port.IOrderRepository;
import fr.unice.polytech.application.port.IRestaurantRepository;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantService;
import fr.unice.polytech.domain.exceptions.DiscountStrategyAlreadyExistsException;
import fr.unice.polytech.domain.models.Address;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.order.OrderStatus;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import fr.unice.polytech.domain.models.restaurant.discountstrategy.DiscountStrategy;
import fr.unice.polytech.infrastructure.repository.inmemory.OrderRepository;
import fr.unice.polytech.utils.Utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class RestaurantService implements IRestaurantService {
    private IRestaurantRepository restaurantRepository;
    private IOrderRepository orderRepository = new OrderRepository();
    Logger logger = Logger.getLogger(RestaurantService.class.getName());

    public RestaurantService(IRestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public int getNumberOfOrdersPending(Schedule schedule) {
        List<Order> orders = orderRepository.findAll();
        return (int) orders.stream()
                .filter(order -> order.getStatus().equals(OrderStatus.PENDING))
                .filter(order -> order.getOrderTime().toLocalTime().isAfter(schedule.getStartTime()) &&
                        order.getOrderTime().toLocalTime().isBefore(schedule.getEndTime()))
                .count();
    }
    @Override
    public List<Restaurant> getAvailableRestaurants(LocalDateTime dateTime) {
        List<Restaurant> availableRestaurants = new ArrayList<>();
        List<Restaurant> allRestaurants = restaurantRepository.findAll();
        for (Restaurant restaurant : allRestaurants) {
            try {
                Schedule schedule = restaurant.getScheduleByDay(dateTime);
                int currentCapacity = restaurant.getCurrentCapacity(schedule);
                if (restaurant.isOpenAt(dateTime) && currentCapacity > 0 && getNumberOfOrdersPending(schedule) < currentCapacity) {
                    availableRestaurants.add(restaurant);
                }
            }catch (IllegalArgumentException e){
                logger.warning(restaurant.getName() + e.getMessage());
            }

        }
        return availableRestaurants;
    }

    @Override
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    @Override
    public List<Restaurant> getUnavailableRestaurants(LocalDateTime orderTime) {
        List<Restaurant> allRestaurants = restaurantRepository.findAll();
        List<Restaurant> availableResto = getAvailableRestaurants(orderTime);
        return allRestaurants.stream()
                .filter(restaurant -> !availableResto.contains(restaurant))
                .toList();
    }

    @Override
    public Restaurant getRestaurantById(String restaurantId) throws EntityNotFoundException {
        return restaurantRepository.findById(restaurantId).orElseThrow(
                () -> new EntityNotFoundException("Restaurant", restaurantId)
        );
    }


    @Override
    public Restaurant createRestaurant(String restaurantName, String street, String city, String zipCode, String country) {
        Address address = new Address(street, city, zipCode, country);
        // tuple (name, address) should be unique
        restaurantRepository.findByNameAndAddress(restaurantName, address)
                .ifPresent(restaurant -> {
                    throw new IllegalArgumentException("Restaurant with name " + restaurantName + " and address " + address + " already exists");
                });
        String id = Utils.generateUniqueId();
        Restaurant restaurant = new Restaurant(id, restaurantName, address);
        return restaurantRepository.add(restaurant);
    }

    //TO DO: delete safely this method and use findsByName instead
    @Override
    public Optional<Restaurant> findByName(String restaurantName) {
        return restaurantRepository.findByName(restaurantName)
                .stream()
                .findFirst();
    }

    @Override
    public List<Restaurant> findsByName(String restaurantName) {
        return restaurantRepository.findsByName(restaurantName);
    }

    @Override
    public void updateRestaurantMenu(String restaurantId, List<MenuItem> menuItems) throws EntityNotFoundException {
        // Récupérer le restaurant par son ID
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(
                () -> new EntityNotFoundException("updateRestaurantMenu method - Restaurant", restaurantId)
        );

        restaurant.setMenu(menuItems);
        restaurantRepository.update(restaurant);
    }


    @Override
    public void addDiscountStrategy(String restaurantId, DiscountStrategy discountStrategy) throws EntityNotFoundException, DiscountStrategyAlreadyExistsException {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(
                () -> new EntityNotFoundException("addDiscountStrategy method - Restaurant", restaurantId)
        );
        restaurant.addDiscountStrategy(discountStrategy);
        restaurantRepository.update(restaurant);
    }

    public void setOrderRepository(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository ;
    }

    public void setRestaurantRepository(IRestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }
}