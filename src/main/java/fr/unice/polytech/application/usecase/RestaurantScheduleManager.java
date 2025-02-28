package fr.unice.polytech.application.usecase;

import fr.unice.polytech.application.exceptions.EntityNotFoundException;
import fr.unice.polytech.application.exceptions.RestaurantNotFoundException;
import fr.unice.polytech.application.port.IRestaurantRepository;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantScheduleManager;
import fr.unice.polytech.domain.exceptions.InvalidScheduleException;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.Schedule;

import java.time.DayOfWeek;
import java.util.*;

/**
 * This is a facade class that provides methods to manage restaurant schedules.
 */
public class RestaurantScheduleManager implements IRestaurantScheduleManager {
    private final IRestaurantRepository restaurantRepository;

    public RestaurantScheduleManager(IRestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    private Restaurant getRestaurant(String restaurantId) throws EntityNotFoundException {
        return restaurantRepository.findById(restaurantId).orElseThrow(
                () -> new EntityNotFoundException("Restaurant", restaurantId)
        );
    }

    /**
     * Add or update a schedule for the restaurant.<br>
     * Eg. if the restaurant is open from 12:00 to 14:00 with 2 staff, and a new schedule is added from 13:00 to 15:00 with 3 staff,
     * the existing schedule will be split into two: 12:00 - 13:00 with 2 staff, and 13:00 - 14:00 with 3 staff.
     * otherwise, the new schedule will be added.<br>
     * NOTE: Adjacent schedules are automatically merged <br>
     * E.g. 12:00 - 13:00 and 13:00 - 14:00 with 2 staff will be merged into 12:00 - 14:00 with 2 staff
     * @param newSchedule The new schedule to add
     */
    @Override
    public void addSchedule(String restaurantId, Schedule newSchedule) throws InvalidScheduleException, EntityNotFoundException {
        Restaurant restaurant = getRestaurant(restaurantId);
        // Handle overlapping schedules and split if necessary
        handleOverlappingSchedules(restaurant, newSchedule);
        // Add the new schedule after processing overlaps
        restaurant.addSchedule(newSchedule);
        restaurantRepository.update(restaurant);
    }

    private void handleOverlappingSchedules(Restaurant restaurant, Schedule newSchedule) {
        List<Schedule> overlappingSchedules = findOverlappingSchedules(restaurant, newSchedule);
        if (overlappingSchedules.isEmpty()) { return;}
        List<Schedule> schedulesToAdd = new ArrayList<>();
        for (Schedule existingSchedule : overlappingSchedules) {
            // If schedules are identical, no update is needed
            if (existingSchedule.equals(newSchedule)) {
                throw new InvalidScheduleException("Schedule already exist.");
            }
            // Handle splitting of overlapping schedules
            splitOverlappingSchedules(newSchedule, existingSchedule, schedulesToAdd);
            // Remove the old overlapping schedule
            restaurant.removeSchedule(existingSchedule);
        }
        // Add split schedules
        schedulesToAdd.forEach(restaurant::addSchedule);
    }

    private void splitOverlappingSchedules(Schedule newSchedule, Schedule existingSchedule, List<Schedule> schedulesToAdd) {
        // If the existing schedule starts before the new one, create a split before the new one
        if (existingSchedule.getStartTime().isBefore(newSchedule.getStartTime())) {
            schedulesToAdd.add(new Schedule(
                    existingSchedule.getDay(),
                    existingSchedule.getStartTime(),
                    newSchedule.getStartTime(),
                    existingSchedule.getNumberOfWorkingStaff()
            ));
        }
        // If the existing schedule ends after the new one, create a split after the new one
        if (existingSchedule.getEndTime().isAfter(newSchedule.getEndTime())) {
            schedulesToAdd.add(new Schedule(
                    existingSchedule.getDay(),
                    newSchedule.getEndTime(),
                    existingSchedule.getEndTime(),
                    existingSchedule.getNumberOfWorkingStaff()
            ));
        }
    }

    private List<Schedule> findOverlappingSchedules(Restaurant restaurant, Schedule newSchedule) {
        // Logic to find overlapping schedules in the restaurant's current schedules
        return restaurant.getSchedules().stream()
                .filter(schedule -> schedule.overlapsWith(newSchedule))
                .toList();
    }

    @Override
    public Optional<List<Schedule>> getSchedules(String restaurantId) throws RestaurantNotFoundException {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(
                () -> new RestaurantNotFoundException("Restaurant not found")
        );
        return Optional.of(restaurant.getSchedules());
    }

    @Override
    public Schedule getScheduleByRestaurantId(String restaurantId, String scheduleId) {
        Optional<Restaurant> restaurant = restaurantRepository.findById(restaurantId);
        if (restaurant.isPresent()) {
            return restaurant.get().getScheduleById(scheduleId);
        } else {
            return null;
        }
    }

    @Override
    public Optional<List<Schedule>> getSchedulesByDay(String restaurantId, String day) {
        Restaurant restaurant = getRestaurant(restaurantId);
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(day.toUpperCase());
        return restaurant.getSchedules().stream()
                .filter(schedule -> schedule.getDay().equals(dayOfWeek))
                .findAny()
                .map(List::of);
    }

    @Override
    public Optional<Schedule> updateSchedule(String restaurantId, Schedule existingSchedule, Schedule newSchedule) throws InvalidScheduleException {
        Restaurant restaurant = getRestaurant(restaurantId);
        restaurant.getSchedules().stream()
                .filter(schedule -> schedule.equals(existingSchedule))
                .findAny()
                .ifPresentOrElse(
                        schedule -> restaurant.updateSchedule(schedule, newSchedule),
                        () -> {
                            throw new InvalidScheduleException("Schedule not found");
                        }
                );
        restaurantRepository.update(restaurant);
        return Optional.of(newSchedule);
    }

    @Override
    public List<Schedule> removeSchedule(String restaurantId, Schedule schedule) throws InvalidScheduleException, RestaurantNotFoundException {
        Restaurant restaurant = getRestaurant(restaurantId);
        restaurant.removeSchedule(schedule);
        restaurantRepository.update(restaurant);
        return restaurant.getSchedules();
    }


}
