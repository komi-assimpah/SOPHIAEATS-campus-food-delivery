package fr.unice.polytech.application.usecase;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import fr.unice.polytech.application.exceptions.EntityNotFoundException;
import fr.unice.polytech.application.port.IRestaurantRepository;
import fr.unice.polytech.domain.exceptions.InvalidScheduleException;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.Schedule;

public class ScheduleService {
    private IRestaurantRepository restaurantRepository;
    private Logger logger = Logger.getLogger(ScheduleService.class.getName());

    public ScheduleService(IRestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    private List<Schedule> findOverlappingSchedules(List<Schedule> openingHours, Schedule newSchedule) {
        return openingHours.stream()
                .filter(existingSchedule -> existingSchedule.getDay() == newSchedule.getDay() && existingSchedule.overlapsWith(newSchedule))
                .toList();
    }

    /**
     * Add or update a schedule for the restaurant.
     * Eg. if the restaurant is open from 12:00 to 14:00 with 2 staff, and a new schedule is added from 13:00 to 15:00 with 3 staff,
     * the existing schedule will be split into two: 12:00 - 13:00 with 2 staff, and 13:00 - 14:00 with 3 staff.
     * otherwise, the new schedule will be added.
     * By default, this method will automatically merge adjacent schedules after adding or updating.
     *
     * @param newSchedule
     * @return
     * @see #addOrUpdateSchedule(String, Schedule, boolean)
     */
    public boolean addOrUpdateSchedule(String restaurantId, Schedule newSchedule) throws InvalidScheduleException {
        return addOrUpdateSchedule(restaurantId, newSchedule, true);
    }

    /**
     * Add or update a schedule for the restaurant.
     * Eg. if the restaurant is open from 12:00 to 14:00 with 2 staff, and a new schedule is added from 13:00 to 15:00 with 3 staff,
     * the existing schedule will be split into two: 12:00 - 13:00 with 2 staff, and 13:00 - 14:00 with 3 staff.
     * otherwise, the new schedule will be added.
     *
     * @param newSchedule
     * @param mergeAdjacent If true, automatically merge adjacent schedules after adding or updating E.g. 12:00 - 13:00 and 13:00 - 14:00 with 2 staff will be merged into 12:00 - 14:00 with 2 staff
     * @return
     */
    public boolean addOrUpdateSchedule(String restaurantId, Schedule newSchedule, boolean mergeAdjacent) throws InvalidScheduleException {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(
                () -> new EntityNotFoundException("Restaurant", restaurantId)
        );
        List<Schedule> openingHours = restaurant.getSchedules();


        List<Schedule> overlappingSchedules = findOverlappingSchedules(openingHours, newSchedule);

        if (overlappingSchedules.isEmpty()){
            restaurant.addSchedule(newSchedule);
            return true;
        } else {
            for (Schedule existingSchedule : overlappingSchedules) {
                // If overlapping but the same staff, skip the update
                if (existingSchedule.equals(newSchedule)) {
                    logger.info(() -> "No update needed: schedule already exists");
                    return false;
                }
                // If partially overlapping, split the existing schedule
                Schedule scheduleSplitted = null;
                if (existingSchedule.getStartTime().isBefore(newSchedule.getStartTime())) {
                    scheduleSplitted = new Schedule(existingSchedule.getDay(),
                            existingSchedule.getStartTime(), newSchedule.getStartTime(), existingSchedule.getNumberOfWorkingStaff());

                }
                if (existingSchedule.getEndTime().isAfter(newSchedule.getEndTime())) {
                    scheduleSplitted = new Schedule(existingSchedule.getDay(),
                            newSchedule.getEndTime(), existingSchedule.getEndTime(), existingSchedule.getNumberOfWorkingStaff());
               }

                restaurant.removeSchedule(existingSchedule);
                restaurant.addSchedule(scheduleSplitted);
                restaurant.addSchedule(newSchedule);
            }
            if (mergeAdjacent) {
                // Automatically merge adjacent schedules after adding or updating
                // E.g. 12:00 - 13:00 and 13:00 - 14:00 with 2 staff will be merged into 12:00 - 14:00 with 2 staff
                restaurant.setOpeningHours(mergeAdjacentSchedules(openingHours));
            }
            return true;

        }
    }
    //TO FIX mergingAdjacentSchedules is managed by the restaurant class at each schedule addition
    private List<Schedule> mergeAdjacentSchedules(List<Schedule> openingHours) {
        // Sort the schedules by start time to ensure adjacent ones are next to each other
        List<Schedule >openHoursCopy = new ArrayList<>(openingHours);

        openHoursCopy.sort(Comparator.comparing(Schedule::getStartTime));

        List<Schedule> mergedSchedules = new ArrayList<>();
        Schedule previous = null;

        for (Schedule current : openHoursCopy) {
            if (previous != null) {
                // Check if the current schedule is adjacent to the previous one and has the same staff
                if (previous.getEndTime().equals(current.getStartTime()) &&
                        previous.getDay().equals(current.getDay()) &&
                        previous.getNumberOfWorkingStaff() == current.getNumberOfWorkingStaff()) {

                    // Merge the two schedules by extending the previous one's end time
                    previous.setEndTime(current.getEndTime());
                } else {
                    // Add the previous schedule to the list if no merge occurred
                    mergedSchedules.add(previous);
                    previous = current;
                }
            } else {
                previous = current;
            }
        }

        // Add the last remaining schedule
        if (previous != null) {
            mergedSchedules.add(previous);
        }
        return mergedSchedules;
    }

    public boolean isRestaurantOpen(String restaurantId, LocalDateTime orderTime) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(
                () -> new EntityNotFoundException("Restaurant", restaurantId)
        );
        return restaurant.isOpenAt(orderTime);
    }
}