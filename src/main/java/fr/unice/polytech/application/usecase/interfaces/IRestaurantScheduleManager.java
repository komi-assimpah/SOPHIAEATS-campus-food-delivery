package fr.unice.polytech.application.usecase.interfaces;


import fr.unice.polytech.application.exceptions.RestaurantNotFoundException;
import fr.unice.polytech.domain.exceptions.InvalidScheduleException;
import fr.unice.polytech.domain.models.restaurant.Schedule;

import java.util.List;
import java.util.Optional;

public interface IRestaurantScheduleManager {
    /**
     * Method to add a new schedule to a restaurant
     */
    void addSchedule(String restaurantId, Schedule schedule) throws IllegalArgumentException, InvalidScheduleException, RestaurantNotFoundException;

    /**
     * Method to get all schedules of a restaurant
     * @return  The schedules of the restaurant
     */
    Optional<List<Schedule>> getSchedules(String restaurantId) throws RestaurantNotFoundException;


    Schedule getScheduleByRestaurantId(String restaurantId, String scheduleId);




        /**
         * Method to get the schedule of a restaurant on a specific day
         * @return  All schedules of the restaurant on the specified day
         */
    Optional<List<Schedule>> getSchedulesByDay(String restaurantId, String day) throws RestaurantNotFoundException;

    /**
     * Method to update an existing schedule of a restaurant
     * @return  The updated schedule if it was updated successfully
     */
    Optional<Schedule> updateSchedule(String restaurantId, Schedule existingSchedule, Schedule newSchedule) throws InvalidScheduleException, RestaurantNotFoundException;

    /**
     * Method to remove a schedule from a restaurant
     * @return  All schedules of the restaurant after removing the specified schedule
     */
    List<Schedule> removeSchedule(String restaurantId, Schedule schedule) throws InvalidScheduleException, RestaurantNotFoundException;


}
