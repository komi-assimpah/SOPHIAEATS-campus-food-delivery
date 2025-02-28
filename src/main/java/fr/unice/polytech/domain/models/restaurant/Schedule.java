package fr.unice.polytech.domain.models.restaurant;

import java.time.*;
import java.util.*;
import java.util.logging.Logger;

import fr.unice.polytech.application.usecase.OrderService;
import fr.unice.polytech.domain.exceptions.InvalidScheduleException;
import fr.unice.polytech.domain.models.order.OrderItem;

public class Schedule {
    private String id;
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;
    private int numberOfWorkingStaff;
    private int currentLoad;
    private int capacityOfProduction;
    private static final int AVERAGE_MEAL_PREP_TIME = 30; // in minutes
    private Map<String, List<LocalTime>> reservedTimes = new HashMap<>();
    private Logger logger = Logger.getLogger(Schedule.class.getName());


    public Schedule(DayOfWeek day, LocalTime startTime, LocalTime endTime, int numberOfWorkingStaff) throws InvalidScheduleException {
        validateTimeRange(startTime, endTime);
        validateNumberOfWorkingStaff(numberOfWorkingStaff);
        this.id = UUID.randomUUID().toString();
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.numberOfWorkingStaff = numberOfWorkingStaff;
        this.currentLoad = 0;
        intializeCapacity();
    }

    public Schedule(){

    }

    public Schedule(DayOfWeek day, LocalTime startTime, LocalTime endTime) throws InvalidScheduleException {
        this(day, startTime, endTime, 0);
    }

    private void validateNumberOfWorkingStaff(int numberOfWorkingStaff) throws InvalidScheduleException {
        if (numberOfWorkingStaff < 0) {
            throw new InvalidScheduleException("Number of working staff must be positive");
        }
    }

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) throws InvalidScheduleException {
        if (startTime.isAfter(endTime)) {
            throw new InvalidScheduleException("Start time '" + startTime + "' must be before end time '" + endTime + "'");
        }
    }

    public boolean isWithinTimeSlot(LocalTime time) {
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    public boolean isWithinOpeningHours(LocalDateTime time) {
        return day == time.getDayOfWeek() && isWithinTimeSlot(time.toLocalTime());
    }

    // Helper methods

    public boolean overlapsWith(Schedule other) {
        return day == other.day && (startTime.isBefore(other.endTime) && other.startTime.isBefore(endTime));
        // isAfter and isBefore are both exclusive
        // Example: 08:00 - 12:00 overlaps with 10:00 - 14:00
        // 08:00.isBefore(14:00) -> true
        // 10:00.isBefore(12:00) -> true
        // Example: 08:00 - 12:00 and 12:00 - 14:00 should not be considered as overlapping
        // 08:00.isBefore(14:00) -> true
        // 12:00.isBefore(12:00) -> false
    }

    public LocalTime getEarliestStartTime(Schedule newSchedule) {
        return startTime.isBefore(newSchedule.startTime) ? startTime : newSchedule.startTime;
    }

    /**
     * Latest as in Late (or after) in time
     *
     * @param newSchedule The new schedule to compare with
     * @return The latest end time between the two schedules
     */
    public LocalTime getLatestEndTime(Schedule newSchedule) {
        return endTime.isAfter(newSchedule.endTime) ? endTime : newSchedule.endTime;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getNumberOfWorkingStaff() {
        return numberOfWorkingStaff;
    }

    public int getCurrentLoad() {
        return currentLoad;
    }

    // setters are package-private to only allow the Restaurant class to modify the schedule
    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void setNumberOfWorkingStaff(int numberOfWorkingStaff) throws InvalidScheduleException {
        validateNumberOfWorkingStaff(numberOfWorkingStaff);
        this.numberOfWorkingStaff = numberOfWorkingStaff;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "id='" + id + '\'' +
                ", day=" + day +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", numberOfWorkingStaff=" + numberOfWorkingStaff +
                ", currentLoad=" + currentLoad +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, startTime, endTime, numberOfWorkingStaff);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return numberOfWorkingStaff == schedule.numberOfWorkingStaff
                && day == schedule.day
                && Objects.equals(startTime, schedule.startTime)
                && Objects.equals(endTime, schedule.endTime);
    }


    /**
     * Dynamically calculate the maximum capacity for a given menu item during this schedule.
     * @param menuItemPreparationTime The preparation time of the menu item (in seconds).
     * @return The total number of items that can be prepared during this schedule based on staff count.
     */
    public int computeMaxProductionCapacity(int menuItemPreparationTime) {
        // Calculate the available time slot duration in seconds
        int timeSlotDurationInSeconds = (endTime.toSecondOfDay() - startTime.toSecondOfDay());

        // Calculate how many items each staff member can prepare within the time slot
        int itemsPerStaff = timeSlotDurationInSeconds / menuItemPreparationTime;

        // Total capacity is the number of staff multiplied by how many items each staff member can prepare
        return numberOfWorkingStaff * itemsPerStaff;
    }

    public void intializeCapacity() {
        this.capacityOfProduction = computeMaxProductionCapacity(AVERAGE_MEAL_PREP_TIME);
    }

    public int getCapacityOfProduction() {
        return capacityOfProduction;
    }

    /**
     * Check if the schedule can handle the order based on the current load and preparation times.
     *
     * @param orderItems The list of items in the order.
     * @return True if the schedule has enough capacity to handle the order.
     */
    public boolean canHandleOrder(List<OrderItem> orderItems, LocalDateTime deliveryTime) {
        // Calculate the total remaining available time for the staff in this schedule (in seconds)
        int availableTimePerStaff = (int) Duration.between(deliveryTime.toLocalTime(), endTime).getSeconds();
        int totalRemainingAvailableTime = numberOfWorkingStaff * availableTimePerStaff;

        // The restaurant might be busy with other orders, so we need to consider the current load
        int remainingAvailableTime = totalRemainingAvailableTime - currentLoad;

        // Calculate the total preparation time required for the new order
        int totalRequiredTime = calculateTotalPreparationTimeInSeconds(orderItems);

        // Check if the remaining available time can handle the new order
        return totalRequiredTime <= remainingAvailableTime;
    }


    /**
     * Calculate the total preparation time required for the order based on the quantity of each menu item.
     *
     * @param orderItems The list of items in the order.
     * @return The total preparation time for the order (in seconds).
     */
    public int calculateTotalPreparationTimeInSeconds(List<OrderItem> orderItems) {
        int totalPreparationTime = 0;
        for (OrderItem orderItem : orderItems) {
            totalPreparationTime += orderItem.getItem().getPreparationTimeInMinutes()*60 * orderItem.getQuantity();
        }
        return totalPreparationTime;
    }

    /**
     * Calculate the possible delivery time after an item is added to the order.
     * @param orderItems The list of items in the order
     * @param requestedDeliveryTime The time selected by the customer for delivery
     * @return The earliest possible delivery time
     */
    public LocalTime calculateEarliestDeliveryTime(List<OrderItem> orderItems, LocalDateTime requestedDeliveryTime) {
        // Ensure that requested delivery time falls within restaurant working hours
        LocalTime requestedTime = requestedDeliveryTime.toLocalTime();
        if (requestedTime.isBefore(startTime) || requestedTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Requested delivery time is outside restaurant working hours.");
        }

        // Calculate total preparation time required for the entire order
        int totalPreparationTimeInSeconds = calculateTotalPreparationTimeInSeconds(orderItems);

        // Add the current load (ongoing preparation tasks)
        totalPreparationTimeInSeconds += currentLoad;

        // Adjust preparation time based on the number of working staff
        int adjustedPreparationTime = totalPreparationTimeInSeconds / numberOfWorkingStaff;

        // Calculate when the preparation will finish if started at restaurant start time (12:00 PM or later)
        LocalTime preparationEndTime = startTime.plusSeconds(adjustedPreparationTime);

        // If the preparation end time is earlier than or equal to the requested delivery time, the order can be fulfilled
        if (preparationEndTime.isBefore(requestedTime) || preparationEndTime.equals(requestedTime)) {
            return requestedTime;
        }
        // If the preparation takes longer, return the time when the preparation will actually finish
        return preparationEndTime;
    }

    /**
     * Reduces the capacity based on the order's total preparation time.
     * After an order is placed, the load increases based on the preparation time required.
     */
    public void reduceCapacity(List<OrderItem> orderItems) {
        int totalPreparationTime = calculateTotalPreparationTimeInSeconds(orderItems);
        int totalItems = orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
        this.capacityOfProduction -= totalItems;
        currentLoad += totalPreparationTime;
    }


    public List<LocalDateTime> displayAllDeliveryTimesAvailable(LocalDateTime currentTime) {
        if (Duration.between(currentTime.toLocalTime(), startTime).toMinutes() > 30) {
            throw new IllegalArgumentException("Interval between startTime and endTime is too large.");
        }

        List<LocalDateTime> availableTimes = new ArrayList<>(
                (endTime.toSecondOfDay() - startTime.toSecondOfDay()) / (60 * 30)
        );

        LocalTime timeSlot = startTime;
        List<LocalTime> reservedForDay = reservedTimes.getOrDefault(day.toString(), Collections.emptyList());

        while (timeSlot.isBefore(endTime)) {
            if (timeSlot.isAfter(LocalTime.from(currentTime.plusMinutes(30))) && !reservedForDay.contains(timeSlot)) {
                LocalDateTime deliveryTime = LocalDateTime.of(currentTime.toLocalDate(), timeSlot);
                availableTimes.add(LocalDateTime.from(deliveryTime));
            }
            timeSlot = timeSlot.plusMinutes(30); // Avancer de 30 minutes
        }
        return availableTimes;
    }

    // Méthode pour réserver un créneau
    public boolean reserveTimeSlot(LocalDateTime deliveryTime, LocalDateTime currentTime) {
        String day = deliveryTime.getDayOfWeek().toString();
        if (day.equals(this.day.toString()) && deliveryTime.isAfter(currentTime.plusMinutes(30))) {
            reservedTimes.putIfAbsent(day, new ArrayList<>());
            reservedTimes.get(day).add(LocalTime.from(deliveryTime));
            return true;
        }
        throw new IllegalArgumentException("Delivery time is not available");
    }

    public LocalDateTime getAvailableDeliveryTime(LocalDateTime orderTime) {
        List<LocalDateTime> availableTimes = displayAllDeliveryTimesAvailable(orderTime);
        return availableTimes.getFirst();
    }

    public List<LocalDateTime> getAllAvailableDeliveryTime(LocalDateTime orderTime) {
        List<LocalDateTime> availableTimes = displayAllDeliveryTimesAvailable(orderTime);
        return availableTimes;
    }
}
