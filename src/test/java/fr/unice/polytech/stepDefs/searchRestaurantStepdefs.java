package fr.unice.polytech.stepDefs;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fr.unice.polytech.application.port.IRestaurantRepository;
import fr.unice.polytech.application.usecase.RestaurantService;
import fr.unice.polytech.domain.models.Address;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import fr.unice.polytech.domain.models.user.User;
import fr.unice.polytech.domain.models.user.UserStatus;
import fr.unice.polytech.infrastructure.repository.inmemory.RestaurantRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class searchRestaurantStepdefs {

    private User campusUser;
    private List<Restaurant> availableRestaurants;
    private IRestaurantRepository restaurantRepository;
    private RestaurantService restaurantService;
    private LocalDateTime searchTime;

    private Optional<Restaurant> restaurantSearched;
    private Restaurant restaurant1;
    private Restaurant restaurant2;
    private String id1;
    private String id2;
    private String name1;
    private String name2;
    private Address address1;
    private Address address2;
    private MenuItem item1;
    private MenuItem item2;
    private Schedule schedule1;
    private Schedule schedule2;

    @Given("the available restaurants list is exhaustive, I want to do a searching to get my restaurant quicker on MONDAY at {int}:{int}")
    public void theAvailableRestaurantsListIsExhaustiveIWantToDoASearchingToGetMyRestaurantQuickerOnMONDAYAt(int hour, int mins) {
        campusUser = new User("John Doe", "johndoe@email.com", "password");
        campusUser.setType(UserStatus.CAMPUS_STUDENT);
        availableRestaurants = new ArrayList<>();

        restaurantRepository = new RestaurantRepository();
        restaurantService = new RestaurantService(restaurantRepository);
        searchTime = LocalDateTime.of(2024, 10, 21, hour, mins);
    }

    @When("I am app home page again")
    public void iAmAppHomePageAgain() {
        id1 = "123";
        id2 = "456";
        name1 = "Subwaya";
        name2 = "Régal";
        address1 = new Address("930 Route des Colles", "06410", "Biot", "France");
        address2 = new Address("992 Route des Colles", "06410", "Biot", "France");
        item1 = new MenuItem("ChocoBon", 10.0, 15);
        item2 = new MenuItem("Pizza", 8.0, 20);

        schedule1 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), 3);
        schedule2 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(15, 0), 3);

        restaurant1 = new Restaurant(id1, name1, address1);
        restaurant2 = new Restaurant(id2, name2, address2);
        restaurant1.addSchedule(schedule1);
        restaurant2.addSchedule(schedule2);

        restaurantRepository.add(restaurant1);
        restaurantRepository.add(restaurant2);

        // Re-initialiser restaurantService pour inclure les nouvelles données ajoutées
        restaurantService = new RestaurantService(restaurantRepository);


    }

    @And("I type {string} in the search bar")
    public void andSearchRestoByName(String restaurantName) {

//        for (Restaurant r : restaurantService.getAllRestaurants()) {
//            System.out.println("Restaurant: " + r.getName());
//            System.out.println("Schedules: " + r.getSchedules());
//        }

        restaurantSearched = restaurantService.findByName(restaurantName);
        assertTrue(restaurantSearched.isPresent());
    }


    @Then("I should get the restaurant {string} not being selectable because it's closed")
    public void iShouldGetTheRestaurantNotBeingSelectableBecauseItSClosed(String arg0) {
        List<Restaurant> unavResto =  restaurantService.getUnavailableRestaurants(searchTime);

//        System.out.println("=====================================");
//        System.out.println("Unavailable Restaurants:");
//        for (Restaurant r : unavResto) {
//            System.out.println("Restaurant: " + r.getName());
//            System.out.println("Schedules: " + r.getSchedules());
//        }

        assertTrue(unavResto.stream().anyMatch(r -> r.equals(restaurantSearched.get())));
    }


    @Then("I should restaurant {string} being selectable because it's still opened")
    public void iShouldRestaurantBeingSelectableBecauseItSStillOpened(String restoName) {
        List<Restaurant> avResto =  restaurantService.getAvailableRestaurants(searchTime);
//        System.out.println("=====================================");
//        System.out.println("Available Restaurants:");
//        for (Restaurant r : avResto) {
//            System.out.println("Restaurant: " + r.getName());
//            System.out.println("Schedules: " + r.getSchedules());
//        }
        assertTrue(avResto.stream().anyMatch(r -> r.equals(restaurantSearched.get())));
    }
}