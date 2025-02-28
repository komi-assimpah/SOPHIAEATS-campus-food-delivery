package fr.unice.polytech.application.port;

import java.util.List;
import java.util.Optional;

import fr.unice.polytech.domain.models.Address;
import fr.unice.polytech.domain.models.restaurant.Restaurant;

public interface IRestaurantRepository extends IReadRepository<Restaurant>, IWriteRepository<Restaurant> {
    /**
     * Method to find a restaurant by its name
     * NOTE: multiple restaurants can have the same name but different ids
     */
    List<Restaurant> findByName(String restaurantName);

    List<Restaurant> findsByName(String restaurantName);

    Optional<Restaurant> findByNameAndAddress(String restaurantName, Address address);
}