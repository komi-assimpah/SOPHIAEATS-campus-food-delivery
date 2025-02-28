package fr.unice.polytech.junit.application.usecase;

import fr.unice.polytech.application.exceptions.EntityNotFoundException;
import fr.unice.polytech.application.port.IRestaurantRepository;
import fr.unice.polytech.application.usecase.ScheduleService;
import fr.unice.polytech.domain.exceptions.InvalidScheduleException;
import fr.unice.polytech.domain.models.Address;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import fr.unice.polytech.infrastructure.repository.inmemory.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleServiceTest {

    private ScheduleService scheduleService;
    private IRestaurantRepository restaurantRepository;

    private Restaurant restaurant;
    private String restaurantId;
    private Schedule schedule1;
    private Schedule schedule2;
    private Address address;

    @BeforeEach
    void setUp() {
        restaurantRepository = new RestaurantRepository();
        scheduleService = new ScheduleService(restaurantRepository);

        restaurantId = "123";
        address = new Address("930 Route des Colles", "06410", "Biot", "France");
        restaurant = new Restaurant(restaurantId, "Gourmet Place", address);
        schedule1 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0), 3);
        schedule2 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(15, 0), LocalTime.of(18, 0), 3);
        restaurant.addSchedule(schedule1);
        restaurant.addSchedule(schedule2);

        restaurantRepository.add(restaurant);
    }


    @Test
    void testAddOrUpdateSchedule_EntityNotFoundException() {
        String invalidRestaurantId = "999";

        assertThrows(EntityNotFoundException.class, () -> scheduleService.addOrUpdateSchedule(invalidRestaurantId, schedule1));
    }

    @Test
    void testIsRestaurantOpen_Open() {
        LocalDateTime orderTime = LocalDateTime.of(2024, 10, 21, 10, 0); // Lundi 10h00
        assertTrue(scheduleService.isRestaurantOpen(restaurantId, orderTime));
    }

    @Test
    void testIsRestaurantOpen_Closed() {
        LocalDateTime orderTime = LocalDateTime.of(2024, 10, 21, 20, 0); // Lundi 20h00
        assertFalse(scheduleService.isRestaurantOpen(restaurantId, orderTime));
    }

    @Test
    void testIsRestaurantOpen_EntityNotFoundException() {
        String invalidRestaurantId = "999";
        LocalDateTime orderTime = LocalDateTime.of(2024, 10, 21, 10, 0); // Lundi 10h00
        assertThrows(EntityNotFoundException.class, () -> scheduleService.isRestaurantOpen(invalidRestaurantId, orderTime));
    }


    @Test
    void testAddOrUpdateSchedule_NoOverlap() throws InvalidScheduleException {
        Schedule newSchedule = new Schedule(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(15, 0), 3);

        boolean result = scheduleService.addOrUpdateSchedule(restaurantId, newSchedule);
        assertTrue(result);
        assertEquals(3, restaurant.getSchedules().size());
        assertTrue(restaurant.getSchedules().contains(newSchedule));
    }

    @Test
    void addOrUpdateSchedule_EmptyScheduleList() throws InvalidScheduleException {
        Restaurant emptyRestaurant = new Restaurant("456", "Empty Place", address);
        restaurantRepository.add(emptyRestaurant);

        Schedule newSchedule = new Schedule(DayOfWeek.WEDNESDAY, LocalTime.of(10, 0), LocalTime.of(15, 0), 3);

        boolean result = scheduleService.addOrUpdateSchedule("456", newSchedule);

        assertTrue(result);
        assertEquals(1, emptyRestaurant.getSchedules().size());
        assertTrue(emptyRestaurant.getSchedules().contains(newSchedule));
    }



    @Test
    void testAddOrUpdateSchedule_OverlappingScheduleMerged() throws InvalidScheduleException {
        Schedule overlappingSchedule = new Schedule(DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(14, 0), 3);

        boolean result = scheduleService.addOrUpdateSchedule(restaurantId, overlappingSchedule, true);

        assertTrue(result);
        assertEquals(2, restaurant.getSchedules().size()); // Should merge with existing schedules
    }

    @Test
    void testAddOrUpdateSchedule_OverlappingScheduleMerged2() throws InvalidScheduleException {
        Schedule overlappingSchedule = new Schedule(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(11, 0), 3);

        boolean result = scheduleService.addOrUpdateSchedule(restaurantId, overlappingSchedule);

        assertTrue(result);
        assertEquals(2, restaurant.getSchedules().size()); // Should merge with existing schedules
    }

    @Test
    void testAddOrUpdateSchedule_OverlappingScheduleMerged3() throws InvalidScheduleException {
        Schedule overlappingSchedule = new Schedule(DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(13, 0), 3);

        boolean result = scheduleService.addOrUpdateSchedule(restaurantId, overlappingSchedule);

        assertTrue(result);
        assertEquals(2, restaurant.getSchedules().size()); // Should merge with existing schedules
    }

    @Test
    void addOrUpdateSchedule_OverlappingScheduleNotMerged() throws InvalidScheduleException {
        Schedule overlappingSchedule = new Schedule(DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(14, 0), 4);

        boolean result = scheduleService.addOrUpdateSchedule(restaurantId, overlappingSchedule);

        assertTrue(result);
        assertEquals(3, restaurant.getSchedules().size());
        assertTrue(restaurant.getSchedules().contains(overlappingSchedule));
    }

    @Test
    void testAddOrUpdateSchedule_ExactMatch() throws InvalidScheduleException {
        boolean result = scheduleService.addOrUpdateSchedule(restaurantId, schedule1, true);

        assertFalse(result); // No change expected
        assertEquals(2, restaurant.getSchedules().size());
    }




}
