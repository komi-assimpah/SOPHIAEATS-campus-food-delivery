
package fr.unice.polytech.infrastructure.repository.inmemory;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.unice.polytech.application.port.IRestaurantRepository;
import fr.unice.polytech.domain.exceptions.InvalidScheduleException;
import fr.unice.polytech.domain.models.Address;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import fr.unice.polytech.infrastructure.repository.exceptions.EntityAlreadyExistsException;

/**
 * RestaurantRepository mimics the behavior of a database access
 * It stores the restaurants in a map for now
 */
public class RestaurantRepository implements IRestaurantRepository {
    private final Map<String, Restaurant> restaurants;

    public RestaurantRepository() {
        restaurants = new HashMap<>();
        // Add some dummy data
        initInMemoryDB();
    }

    private void initInMemoryDB() {
        try {
            Address macDoAddress = new Address("930 Route des Colles,06410,Biot,France");
            Address kfcAddress = new Address("930 Route des Colles,06410,Biot,France");

            Restaurant macDo = new Restaurant("1", "McDonald's", macDoAddress);
            Restaurant kfc = new Restaurant("2", "KFC", kfcAddress);

            MenuItem menuItem1 = new MenuItem("1", "Caesar", 5.0, 2);
            MenuItem menuItem2 = new MenuItem("2", "Fries", 3.0, 1);
            MenuItem menuItem3 = new MenuItem("3", "Coke", 1.0, 1);
            MenuItem menuItem4 = new MenuItem("4", "Chicken", 8.0, 5);
            macDo.addMenuItem(menuItem1);
            macDo.addMenuItem(menuItem2);
            macDo.addMenuItem(menuItem3);
            kfc.addMenuItem(menuItem2);
            kfc.addMenuItem(menuItem3);
            kfc.addMenuItem(menuItem4);

            // Set restaurant schedules
            Schedule schedule1 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(19, 0), 4);
            Schedule schedule2 = new Schedule(DayOfWeek.TUESDAY, LocalTime.of(12, 0), LocalTime.of(19, 30), 2);
            Schedule schedule3 = new Schedule(DayOfWeek.TUESDAY, LocalTime.of(12, 0), LocalTime.of(19, 0), 3);
            macDo.addSchedule(schedule1);
            macDo.addSchedule(schedule2);
            kfc.addSchedule(schedule1);
            kfc.addSchedule(schedule3);

            restaurants.put(macDo.getId(), macDo);
            restaurants.put(kfc.getId(), kfc);
        } catch (InvalidScheduleException e) {
            e.printStackTrace();
        }
    }

    // Restaurant persistence methods
    @Override
    public Restaurant add(Restaurant restaurant) throws EntityAlreadyExistsException {
        if (restaurants.containsKey(restaurant.getId())) {
            throw new EntityAlreadyExistsException("Restaurant with id " + restaurant.getId() + " already exists");
        }
        restaurants.put(restaurant.getId(), restaurant);
        return restaurant;
    }

    @Override
    public void remove(Restaurant restaurant) {
        restaurants.remove(restaurant.getId());
    }

    @Override
    public void update(Restaurant restaurant) {
        restaurants.put(restaurant.getId(), restaurant);
    }

    @Override
    public Optional<Restaurant> findById(String id) {
        return Optional.ofNullable(restaurants.get(id));
    }

    @Override
    public List<Restaurant> findAll() {
        return List.copyOf(restaurants.values());
    }

    @Override
    public List<Restaurant> findByName(String restaurantName) {
        return restaurants.values().stream()
                .filter(r -> r.getName().equals(restaurantName))
                .toList();
    }

    @Override
    public List<Restaurant> findsByName(String restaurantName) {
        String searchName = restaurantName.toLowerCase();
        return restaurants.values().stream()
                .filter(r -> r.getName().toLowerCase().contains(searchName))
                .toList();
    }

    @Override
    public Optional<Restaurant> findByNameAndAddress(String restaurantName, Address address) {
        return restaurants.values().stream()
                .filter(r -> r.getName().equals(restaurantName) && r.getAddress().equals(address))
                .findFirst();
    }
}
