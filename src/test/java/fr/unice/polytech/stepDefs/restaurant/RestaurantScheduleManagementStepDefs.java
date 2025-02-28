package fr.unice.polytech.stepDefs.restaurant;

import static org.junit.Assert.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import fr.unice.polytech.application.exceptions.RestaurantNotFoundException;
import fr.unice.polytech.application.port.IRestaurantRepository;
import fr.unice.polytech.application.usecase.RestaurantScheduleManager;
import fr.unice.polytech.application.usecase.RestaurantService;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantScheduleManager;
import fr.unice.polytech.domain.exceptions.InvalidScheduleException;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import fr.unice.polytech.infrastructure.repository.inmemory.RestaurantRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class RestaurantScheduleManagementStepDefs {
    private Restaurant restaurant;
    private IRestaurantScheduleManager restaurantScheduleManager;
    private RestaurantService restaurantService;
    private IRestaurantRepository restaurantRepository;
    private Exception exception;



    @Before
    public void setUp() {
        restaurantRepository = new RestaurantRepository();
        restaurantService = new RestaurantService(restaurantRepository);
        restaurantScheduleManager = new RestaurantScheduleManager(restaurantRepository);
    }

    @Given("a restaurant named {string}")
    public void aRestaurantNamed(String restaurantName) {
        restaurant = restaurantService.createRestaurant(restaurantName, "123456 Route des Colles", "06410", "Biot", "France");
    }

    @Given("the restaurant has no schedules")
    public void theRestaurantHasNoSchedules() {
        assertTrue(restaurant.getSchedules().isEmpty());
    }

    @Given("the restaurant has the following schedule:")
    public void theRestaurantHasTheFollowingSchedule(List<Map<String, String>> schedulesTable) throws RestaurantNotFoundException, InvalidScheduleException {
        for (Map<String, String> row : schedulesTable) {
            DayOfWeek day = DayOfWeek.valueOf(row.get("Day"));
            LocalTime start = LocalTime.parse(row.get("Start"));
            LocalTime end = LocalTime.parse(row.get("End"));
            int staff = Integer.parseInt(row.get("Staff"));
            restaurantScheduleManager.addSchedule(restaurant.getId(), new Schedule(day, start, end, staff));
        }
    }

    @When("I add a schedule on {string} from {string} to {string} with {int} staff")
    public void i_add_a_schedule_on_from_to_with_staff(String day, String startTime, String endTime, int staffNumber) throws RestaurantNotFoundException {
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(day);
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        try {
            restaurantScheduleManager.addSchedule(restaurant.getId(), new Schedule(dayOfWeek, start, end, staffNumber));
        } catch (InvalidScheduleException e) {
            exception = e;
        }
    }

    @When("I replace the schedule on {string} from {string} to {string} with a new schedule from {string} to {string} with {int} staff")
    public void iReplaceTheSchedule(String day, String oldStartTime, String oldEndTime, String newStartTime, String newEndTime, int newStaff) throws RestaurantNotFoundException, InvalidScheduleException {
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(day);
        LocalTime oldStart = LocalTime.parse(oldStartTime);
        LocalTime oldEnd = LocalTime.parse(oldEndTime);
        LocalTime newStart = LocalTime.parse(newStartTime);
        LocalTime newEnd = LocalTime.parse(newEndTime);


        restaurantScheduleManager.updateSchedule(restaurant.getId(), new Schedule(dayOfWeek, oldStart, oldEnd, newStaff), new Schedule(dayOfWeek, newStart, newEnd, newStaff));
    }

    @When("I remove the schedule on {string} from {string} to {string} with {int} staff")
    public void i_remove_the_schedule_on_from_to(String day, String startTime, String endTime, int nbOfStaff) throws RestaurantNotFoundException, InvalidScheduleException {
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(day);
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        restaurantScheduleManager.removeSchedule(restaurant.getId(), new Schedule(dayOfWeek, start, end, nbOfStaff));
    }

    @Then("the system should display an error message saying {string}")
    public void the_system_should_display_an_error_message_saying(String message) {
        assertEquals(message, exception.getMessage());
    }

    @Then("the restaurant should have the following schedule:")
    public void theRestaurantShouldHaveTheFollowingSchedule(List<Map<String, String>> expectedSchedules) {
        assertSchedulesMatch(expectedSchedules, restaurant.getSchedules());
    }

    @Then("the restaurant should have the following schedules:")
    public void theRestaurantShouldHaveTheFollowingSchedules(List<Map<String, String>> expectedSchedules) {
        assertSchedulesMatch(expectedSchedules, restaurant.getSchedules());
    }

    private void assertSchedulesMatch(List<Map<String, String>> expectedSchedules, List<Schedule> actualSchedules) {
        List<String> actualScheduleList = actualSchedules.stream()
                .map(schedule -> String.format("%s %s %s %d",
                        schedule.getDay(),
                        schedule.getStartTime(),
                        schedule.getEndTime(),
                        schedule.getNumberOfWorkingStaff()))
                .toList();

        List<String> expectedScheduleList = expectedSchedules.stream()
                .map(row -> String.format("%s %s %s %s",
                        row.get("Day"),
                        row.get("Start"),
                        row.get("End"),
                        row.get("Staff")))
                .toList();

        assertEquals(expectedScheduleList, actualScheduleList);
    }
}
