package fr.unice.polytech.stepDefs;

import static org.junit.Assert.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import fr.unice.polytech.application.port.IRestaurantRepository;
import fr.unice.polytech.application.usecase.RestaurantScheduleManager;
import fr.unice.polytech.application.usecase.RestaurantService;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantScheduleManager;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantService;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import fr.unice.polytech.infrastructure.repository.inmemory.RestaurantRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class SelectAvailabeRestaurantStepdefs {
    private List<Restaurant> availableRestaurants;
    private final IRestaurantService restaurantService;
    private final IRestaurantScheduleManager restaurantScheduleManager;

    public SelectAvailabeRestaurantStepdefs() {
        IRestaurantRepository restaurantRepository = new RestaurantRepository();
        restaurantService = new RestaurantService(restaurantRepository);
        restaurantScheduleManager = new RestaurantScheduleManager(restaurantRepository);
    }

    @Given("the following restaurants exist in our system:")
    public void the_following_restaurants_exist_in_our_system(List<Map<String, String>> restaurantData) {
        for (Map<String, String> data : restaurantData) {
            try {
                DayOfWeek dayOfWeek = DayOfWeek.valueOf(data.get("day").toUpperCase());
                LocalTime openingTime = LocalTime.parse(data.get("opening_hours"));
                LocalTime closingTime = LocalTime.parse(data.get("closing_hours"));
                int staffCount = Integer.parseInt(data.get("staff_count"));
                Schedule schedule = new Schedule(dayOfWeek, openingTime, closingTime, staffCount);

                Restaurant resto =  restaurantService.createRestaurant(
                        data.get("name"), data.get("street"), data.get("city"), data.get("zipCode"), data.get("country")
                );
                restaurantScheduleManager.addSchedule(resto.getId(), schedule);
            } catch (Exception e) {
                // Do nothing, handle duplicate restaurant creation
                System.out.println(e.getMessage());
            }
        }
    }

    LocalDateTime tuesday;

    @Given("the user is connected to our application on {string} at {int}:{int}")
    public void i_am_an_user_connected(String day, int hour, int mins) {
        tuesday = LocalDateTime.of(2024, 10, 21, hour, mins);
    }

    @When("the user browses the list of available restaurants")
    public void i_am_app_home_page() {
        availableRestaurants = restaurantService.getAvailableRestaurants(tuesday);
    }

    @Then("the user should see all the available restaurants on MONDAY at {int}:{int}")
    public void iShouldSeeAllTheAvailableRestaurantsOnMondayAt(int hour, int mins) {
        assertFalse(availableRestaurants.isEmpty());
        assertNotNull(availableRestaurants);
        assertTrue(availableRestaurants.stream().allMatch(restaurant -> restaurant.isOpenAt(tuesday)));
    }
}