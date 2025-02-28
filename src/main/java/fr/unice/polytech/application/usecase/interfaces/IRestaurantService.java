package fr.unice.polytech.application.usecase.interfaces;

import fr.unice.polytech.application.exceptions.EntityNotFoundException;
import fr.unice.polytech.application.port.IRestaurantRepository;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.discountstrategy.DiscountStrategy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IRestaurantService {
    List<Restaurant> getAllRestaurants();
    List<Restaurant> getAvailableRestaurants(LocalDateTime dateTime);

    List<Restaurant> getUnavailableRestaurants(LocalDateTime orderTime);
    /**
     * Get a restaurant by its id
     * @param restaurantId the id of the restaurant
     * @return the restaurant with the given id
     * @throws EntityNotFoundException if the restaurant with the given id is not found
     */
    Restaurant getRestaurantById(String restaurantId) throws EntityNotFoundException;

    /**
     * Create a new restaurant
     * @param restaurantName the name of the restaurant
     * @param street the street of the restaurant
     * @param city the city of the restaurant
     * @param zipCode the zip code of the restaurant
     * @param country the country of the restaurant
     * @return the created restaurant
     */
    Restaurant createRestaurant(String restaurantName, String street, String city, String zipCode, String country);

    /**
     * Find a restaurant by its name
     * @param restaurantName the name of the restaurant
     * @return a list of restaurants with the given name
     */
    Optional<Restaurant> findByName(String restaurantName);

    List<Restaurant> findsByName(String restaurantName);

    /**
     * Add a discount strategy to a restaurant
     *
     * @param restaurantId     the id of the restaurant
     * @param discountStrategy the discount strategy to add
     */
    void addDiscountStrategy(String restaurantId, DiscountStrategy discountStrategy);

    void updateRestaurantMenu(String restaurantId, List<MenuItem> menuItems) throws EntityNotFoundException;


    void setRestaurantRepository(IRestaurantRepository restaurantRepository);

}
