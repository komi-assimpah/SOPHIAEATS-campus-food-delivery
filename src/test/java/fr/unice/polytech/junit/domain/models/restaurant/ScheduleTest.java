package fr.unice.polytech.junit.domain.models.restaurant;

import fr.unice.polytech.domain.exceptions.InvalidScheduleException;
import fr.unice.polytech.domain.models.order.OrderItem;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

class ScheduleTest {

    private Schedule schedule;

    @BeforeEach
    void setUp() {
        schedule = new Schedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), 3);
    }

    @Test
    void testGetters() {
        assertEquals(DayOfWeek.MONDAY, schedule.getDay());
        assertEquals(LocalTime.of(9, 0), schedule.getStartTime());
        assertEquals(LocalTime.of(17, 0), schedule.getEndTime());
        assertEquals(3, schedule.getNumberOfWorkingStaff());
        assertEquals(0, schedule.getCurrentLoad());
    }

    @Test
    void testSetters() {
        schedule.setDay(DayOfWeek.TUESDAY);
        schedule.setStartTime(LocalTime.of(8, 0));
        schedule.setEndTime(LocalTime.of(16, 0));
        schedule.setNumberOfWorkingStaff(4);

        assertEquals(DayOfWeek.TUESDAY, schedule.getDay());
        assertEquals(LocalTime.of(8, 0), schedule.getStartTime());
        assertEquals(LocalTime.of(16, 0), schedule.getEndTime());
        assertEquals(4, schedule.getNumberOfWorkingStaff());
    }

    @Test
    void testScheduleCreation() {
        Schedule schedule = new Schedule(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
        assertEquals(DayOfWeek.MONDAY, schedule.getDay());
        assertEquals(LocalTime.of(8, 0), schedule.getStartTime());
        assertEquals(LocalTime.of(12, 0), schedule.getEndTime());
        assertEquals(0, schedule.getNumberOfWorkingStaff());
    }

    @Test
    void testConstructorWithInvalidTimeRange() {
        assertThrows(InvalidScheduleException.class, () ->
                new Schedule(DayOfWeek.MONDAY, LocalTime.of(17, 0), LocalTime.of(9, 0), 3)
        );
    }

    @Test
    void testScheduleCreationWithNumberofStaff() {
        Schedule schedule = new Schedule(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0), 4);
        assertEquals(DayOfWeek.MONDAY, schedule.getDay());
        assertEquals(LocalTime.of(8, 0), schedule.getStartTime());
        assertEquals(LocalTime.of(12, 0), schedule.getEndTime());
        assertEquals(4, schedule.getNumberOfWorkingStaff());
    }


    @Test
    void testInvalidScheduleCreationWithNegativeNumberOfWorkingStaff() {
        try {
            new Schedule(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0), -1);
        } catch (Exception e) {
            assertEquals("Number of working staff must be positive", e.getMessage());
        }
    }

    @Test
    void testInvalidScheduleCreationWithStartTimeAfterEndTime() {
        try {
            new Schedule(DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(8, 0));
        } catch (Exception e) {
            assertEquals("Start time '12:00' must be before end time '08:00'", e.getMessage());
        }
    }



    @Test
    void testOverlaps() {
        Schedule schedule1 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
        Schedule schedule2 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(14, 0));
        assertTrue(schedule1.overlapsWith(schedule2));
    }

    @Test
    void testDoesNotOverlap() {
        Schedule schedule1 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
        Schedule schedule2 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(14, 0));
        assertFalse(schedule1.overlapsWith(schedule2));
    }

    @Test
    void testIsWithinTimeSlot() {
        Schedule schedule = new Schedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), 3);
        assertTrue(schedule.isWithinTimeSlot(LocalTime.of(10, 0)));
        assertFalse(schedule.isWithinTimeSlot(LocalTime.of(18, 0)));
    }

    @Test
    void testIsWithinOpeningHours() {
        Schedule schedule = new Schedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), 3);

        LocalDateTime dateTime = LocalDateTime.of(2024, 10, 21, 10, 0); // Assume Oct 21, 2024 is a Monday
        assertTrue(schedule.isWithinOpeningHours(dateTime));

        dateTime = LocalDateTime.of(2024, 10, 22, 10, 0); // Assume Oct 22, 2024 is a Tuesday
        assertFalse(schedule.isWithinOpeningHours(dateTime));
    }

    @Test
    void testGetEarliestStartTime() {
        Schedule schedule1 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
        Schedule schedule2 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(14, 0));
        assertEquals(LocalTime.of(8, 0), schedule1.getEarliestStartTime(schedule2));

        schedule2 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(7, 0), LocalTime.of(12, 0));
        assertEquals(LocalTime.of(7, 0), schedule1.getEarliestStartTime(schedule2));
    }

    @Test
    void testGetLatestEndTime() {
        Schedule schedule1 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
        Schedule schedule2 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(14, 0));
        assertEquals(LocalTime.of(14, 0), schedule1.getLatestEndTime(schedule2));

        schedule2 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(12, 0));
        assertEquals(LocalTime.of(12, 0), schedule1.getLatestEndTime(schedule2));

        schedule2 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(11, 0));
        assertEquals(LocalTime.of(12, 0), schedule1.getLatestEndTime(schedule2));
    }

    @Test
    void testComputeMaxProductionCapacity() {
        MenuItem menuItem = new MenuItem("Pizza", 10.0, 15);
        int preparationTime = menuItem.getPreparationTimeInSeconds(); // 15 minutes in seconds
        assertEquals(96, schedule.computeMaxProductionCapacity(preparationTime));
    }



//    @Test
//    void testGetDuration() {
//        Schedule schedule = new Schedule(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
//        assertEquals(4 * 60, schedule.getDuration().toMinutes());
//    }

    @Test
    void testScheduleEquality() {
        Schedule schedule1 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
        Schedule schedule2 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
        assertEquals(schedule1, schedule2);
    }

    @Test
    void testScheduleEqualityWithDifferentNumberOfWorkingStaff() {
        Schedule schedule1 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0), 4);
        Schedule schedule2 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0), 5);
        assertNotEquals(schedule1, schedule2);
    }

    @Test
    void testCanHandleOrderWithSufficientCapacity() {
        MenuItem menuItem = new MenuItem("Pizza", 10.0, 15);
        MenuItem menuItem2 = new MenuItem("Pasta", 12.0, 20);

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(menuItem, 2)); // 2 x 15 minutes = 30 minutes
        orderItems.add(new OrderItem(menuItem2, 3)); // 3 x 20 minutes = 60 minutes
        // totalPreparationTime = 30 + 60 = 90 minutes (5400 seconds)

        LocalDateTime deliveryTimeOnMonday = LocalDateTime.of(2024, 10, 21, 14, 0);Schedule schedule1 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(15, 0), 2);

        schedule1.canHandleOrder(orderItems, deliveryTimeOnMonday);
        assertTrue(schedule1.canHandleOrder(orderItems, deliveryTimeOnMonday));
    }

    @Test
    void testCanHandleOrderWithSufficientCapacity2() {
        MenuItem menuItem = new MenuItem("Pizza", 10.0, 15);
        MenuItem menuItem2 = new MenuItem("Pasta", 12.0, 20);

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(menuItem, 2)); // 2 x 15 minutes = 30 minutes
        orderItems.add(new OrderItem(menuItem2, 3)); // 3 x 20 minutes = 60 minutes
        int totalPreparationTime = 30 + 60; // Total 90 minutes (5400 seconds)

        // Schedule with sufficient capacity (9:00 - 17:00 with 3 working staff)
        Schedule schedule1 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), 3);
        LocalDateTime deliveryTime = LocalDateTime.of(2024, 10, 21, 16, 0); // Delivery time at 4:00 PM

        //nb de staff = 3
        assertTrue(schedule1.canHandleOrder(orderItems, deliveryTime));
    }

    @Test
    void testCanHandleOrderWithInsufficientCapacity() {
        MenuItem menuItem = new MenuItem("Burger", 8.0, 20);
        MenuItem menuItem2 = new MenuItem("Salad", 5.0, 15);

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(menuItem, 5)); // 5 x 20 minutes = 100 minutes
        orderItems.add(new OrderItem(menuItem2, 3)); // 3 x 15 minutes = 45 minutes
        int totalPreparationTime = 100 + 45; // Total 145 minutes (8700 seconds)

        // Schedule with insufficient capacity (12:00 - 15:00 with 1 staff member)
        Schedule schedule = new Schedule(DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(15, 0), 1);
        LocalDateTime deliveryTime = LocalDateTime.of(2024, 10, 21, 14, 30); // Delivery time at 2:30 PM

        assertFalse(schedule.canHandleOrder(orderItems, deliveryTime));
    }

    @Test
    void testCanHandleOrderWithExactCapacity() {
        MenuItem menuItem = new MenuItem("Sandwich", 6.0, 10);
        MenuItem menuItem2 = new MenuItem("Soup", 4.0, 15);

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(menuItem, 3)); // 3 x 10 minutes = 30 minutes
        orderItems.add(new OrderItem(menuItem2, 2)); // 2 x 15 minutes = 30 minutes
        int totalPreparationTime = 30 + 30; // Total 60 minutes (3600 seconds)

        // Schedule with exact capacity (12:00 - 13:00 with 1 staff member)
        Schedule schedule = new Schedule(DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(13, 0), 1);
        LocalDateTime deliveryTime = LocalDateTime.of(2024, 10, 21, 13, 0); // Delivery time exactly at the end

        assertFalse(schedule.canHandleOrder(orderItems, deliveryTime));
    }

    @Test
    void testCanHandleOrderWithCurrentLoadAlreadyPartiallyOccupied() {
        MenuItem menuItem = new MenuItem("Pizza", 10.0, 30);
        MenuItem menuItem2 = new MenuItem("Pasta", 12.0, 25);

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(menuItem, 2)); // 2 x 30 minutes = 60 minutes
        orderItems.add(new OrderItem(menuItem2, 1)); // 1 x 25 minutes = 25 minutes
        int totalPreparationTime = 60 + 25; // Total 85 minutes (5100 seconds)

        // Schedule from 11:00 - 16:00 with 1 staff member and existing load of 45 minutes
        Schedule schedule1 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(16, 0), 1);
        schedule1.reduceCapacity(orderItems); // Reduce capacity by 85 minutes
        LocalDateTime deliveryTime = LocalDateTime.of(2024, 10, 21, 15, 0); // Delivery before end time
        assertFalse(schedule1.canHandleOrder(orderItems, deliveryTime));
    }

    @Test
    void testCanHandleOrderExceedingAdjustedCapacityWithLimitedStaff() {
        MenuItem menuItem = new MenuItem("Fish", 15.0, 50);
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(menuItem, 3)); // 3 x 50 minutes = 150 minutes
        int totalPreparationTime = 150 * 60; // Total 9000 seconds

        // Schedule from 9:00 - 12:00 with 1 staff member
        Schedule schedule = new Schedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0), 1);
        LocalDateTime deliveryTime = LocalDateTime.of(2024, 10, 21, 11, 30); // Delivery at 11:30 AM

        assertFalse(schedule.canHandleOrder(orderItems, deliveryTime));
    }


    @Test
    void testReduceCapacityCorrectlyUpdatesCurrentLoad() {
        MenuItem menuItem = new MenuItem("Sandwich", 5.0, 10);
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(menuItem, 2)); // 2 x 10 minutes = 20 minutes

        int initialLoad = schedule.calculateTotalPreparationTimeInSeconds(orderItems);
        schedule.reduceCapacity(orderItems);

        assertEquals(initialLoad, schedule.getCurrentLoad());
    }

    @Test
    void testCalculateEarliestDeliveryTimeWithinCapacity() {
        MenuItem menuItem = new MenuItem("Pasta", 8.0, 15);
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(menuItem, 2)); // 2 x 15 minutes = 30 minutes

        LocalDateTime requestedDeliveryTime = LocalDateTime.of(2024, 10, 21, 14, 30); // within working hours
        LocalTime earliestDelivery = schedule.calculateEarliestDeliveryTime(orderItems, requestedDeliveryTime);

        // Expected to be able to deliver within requested time
        assertEquals(requestedDeliveryTime.toLocalTime(), earliestDelivery);
    }

    @Test
    void testCalculateEarliestDeliveryTimeOutsideWorkingHours() {
        MenuItem menuItem = new MenuItem("Pasta", 8.0, 15);
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(menuItem, 3)); // 3 x 15 minutes = 45 minutes

        // Request delivery time outside the working hours
        LocalDateTime requestedDeliveryTime = LocalDateTime.of(2024, 10, 21, 18, 0);

        // Expect an exception due to delivery time outside schedule
        assertThrows(IllegalArgumentException.class, () -> {
            schedule.calculateEarliestDeliveryTime(orderItems, requestedDeliveryTime);
        });
    }

    @Test
    void testCalculateEarliestDeliveryTimeWhenPreparationExceedsRequestedTime() {
        Schedule schedule1 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), 2);


        MenuItem menuItem1 = new MenuItem("Pizza", 10.0, 60); // 60 minutes
        MenuItem menuItem2 = new MenuItem("Burger", 8.0, 90); // 90 minutes

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(menuItem1, 2)); // Total: 120 minutes
        orderItems.add(new OrderItem(menuItem2, 2)); // Total: 180 minutes
        int totalPreparationTimeInSeconds = (60 + 90)*2 * 60; // Total 18000 seconds (5h 00min)

        // Définir une heure de livraison demandée plus tôt que le temps de préparation requis
        LocalDateTime requestedDeliveryTime = LocalDateTime.of(2024, 10, 21, 11, 0); // Lundi à 11h
        System.out.println(" requestedDeliveryTime" + requestedDeliveryTime);

        // Calculer l'heure de fin de préparation effective
        LocalTime expectedPreparationEndTime = schedule1.getStartTime().plusSeconds(totalPreparationTimeInSeconds/schedule1.getNumberOfWorkingStaff());
        System.out.println(" expectedPreparationEndTime" + expectedPreparationEndTime);

        // Exécuter le test
        LocalTime calculatedDeliveryTime = schedule1.calculateEarliestDeliveryTime(orderItems, requestedDeliveryTime);
        System.out.println(" calculatedDeliveryTime" + calculatedDeliveryTime);

        // Vérifier que le temps de fin de préparation est renvoyé car il dépasse le temps de livraison demandé
        assertNotEquals(requestedDeliveryTime.toLocalTime(), calculatedDeliveryTime);
        assertEquals(expectedPreparationEndTime, calculatedDeliveryTime);
    }




}