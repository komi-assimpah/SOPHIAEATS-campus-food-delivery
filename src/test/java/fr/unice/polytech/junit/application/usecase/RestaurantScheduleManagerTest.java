package fr.unice.polytech.junit.application.usecase;

import fr.unice.polytech.application.exceptions.EntityNotFoundException;
import fr.unice.polytech.application.exceptions.RestaurantNotFoundException;
import fr.unice.polytech.application.port.IRestaurantRepository;
import fr.unice.polytech.application.usecase.RestaurantScheduleManager;
import fr.unice.polytech.domain.exceptions.InvalidScheduleException;
import fr.unice.polytech.domain.models.Address;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestaurantScheduleManagerTest {
    private IRestaurantRepository restaurantRepository;
    private RestaurantScheduleManager restaurantScheduleManager;

    @BeforeEach
    public void setUp() {
        restaurantRepository = Mockito.mock(IRestaurantRepository.class);
        restaurantScheduleManager = new RestaurantScheduleManager(restaurantRepository);
    }

    @Test
    void testAddScheduleSuccessfully() throws InvalidScheduleException {
        // Arrange
        Address address = new Address("930 Route des Colles", "06410", "Biot", "France");

        Restaurant restaurant = new Restaurant("1", "Test Restaurant", address);
        Schedule schedule = new Schedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));

        // Mock the repository behavior
        when(restaurantRepository.findById("1")).thenReturn(java.util.Optional.of(restaurant));

        // Act
        restaurantScheduleManager.addSchedule("1", schedule);

        // Assert
        assertEquals(1, restaurant.getSchedules().size());
        assertEquals(schedule, restaurant.getSchedules().getFirst());
        verify(restaurantRepository, times(1)).update(restaurant);
    }

    @Test
    void testInvalidScheduleThrowsInvalidScheduleException() {
        // Arrange, Act and Assert
        DayOfWeek day = DayOfWeek.MONDAY;
        LocalTime startTime = LocalTime.of(17, 0);
        LocalTime endTime = LocalTime.of(9, 0);
        Exception exception = assertThrows(InvalidScheduleException.class, () ->
                new Schedule(day, startTime, endTime) // Invalid schedule
        );
        assertEquals("Start time '17:00' must be before end time '09:00'", exception.getMessage());
    }

    @Test
    void testAddDuplicateScheduleThrowsInvalidScheduleException(){
        // Arrange
        Address address = new Address("930 Route des Colles", "06410", "Biot", "France");
        Restaurant restaurant = new Restaurant("1", "Test Restaurant", address);
        Schedule schedule = new Schedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));

        // Mock the repository behavior
        when(restaurantRepository.findById("1")).thenReturn(java.util.Optional.of(restaurant));

        // Act and Assert
        assertDoesNotThrow(() -> {
            restaurantScheduleManager.addSchedule("1", schedule);
        });
        Exception exception = assertThrows(InvalidScheduleException.class, () -> {
            restaurantScheduleManager.addSchedule("1", schedule);
        });

        assertEquals("Schedule already exist.", exception.getMessage());
    }

    @Test
    void testRestaurantNotFound() {
        // Arrange
        Schedule schedule = new Schedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));

        // Mock the repository behavior
        when(restaurantRepository.findById("1")).thenReturn(java.util.Optional.empty());

        // Act and Assert
        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            restaurantScheduleManager.addSchedule("1", schedule);
        });

        assertEquals("Restaurant with id 1 not found", exception.getMessage());
    }

    @Test
    void testUpdateExistingSchedule() throws InvalidScheduleException {
        // Arrange
        Address address = new Address("930 Route des Colles", "06410", "Biot", "France");
        Restaurant restaurant = new Restaurant("1", "Test Restaurant", address);
        Schedule existingSchedule = new Schedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        Schedule newSchedule = new Schedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));

        restaurant.addSchedule(existingSchedule);

        // Mock the repository behavior
        when(restaurantRepository.findById("1")).thenReturn(java.util.Optional.of(restaurant));

        // Act
        restaurantScheduleManager.updateSchedule("1", newSchedule, existingSchedule);

        // Assert
        assertEquals(1, restaurant.getSchedules().size());
        assertEquals(newSchedule, restaurant.getSchedules().getFirst());
    }
}
