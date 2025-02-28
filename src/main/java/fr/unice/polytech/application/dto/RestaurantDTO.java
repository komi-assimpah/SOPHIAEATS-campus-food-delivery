package fr.unice.polytech.application.dto;

import fr.unice.polytech.domain.exceptions.InvalidScheduleException;
import fr.unice.polytech.domain.models.Address;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import fr.unice.polytech.domain.models.restaurant.discountstrategy.DiscountStrategy;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RestaurantDTO {
    private String id;
    private String name;
    private Address address;
    private List<MenuItem> menu;
    private List<ScheduleDTO> schedules;
    private List<DiscountStrategy> discountStrategies;

    public RestaurantDTO(Restaurant restaurant) {
        this.id = restaurant.getId() != null ? restaurant.getId() : "";
        this.name = restaurant.getName() != null ? restaurant.getName() : "Unknown Restaurant";
        this.address = restaurant.getAddress() != null ? restaurant.getAddress() : new Address(); // Assuming Address has a default constructor
        this.menu = restaurant.getMenu() != null ? restaurant.getMenu() : Collections.emptyList();
        this.schedules = restaurant.getSchedules() != null ? 
            restaurant.getSchedules().stream().map(ScheduleDTO::new).collect(Collectors.toList()) 
            : Collections.emptyList();
        this.discountStrategies = restaurant.getDiscountStrategies() != null ? restaurant.getDiscountStrategies() : Collections.emptyList();
    }


    public RestaurantDTO(){

    }

    public Restaurant toRestaurant() {
        Address newAddress = new Address(
                this.address.getStreet(),
                this.address.getCity(),
                this.address.getZipCode(),
                this.address.getCountry());

        Restaurant restaurant = new Restaurant(this.id, this.name, newAddress);

        if (this.menu != null) {
            List<MenuItem> menu = new ArrayList<>(this.menu);
            restaurant.setMenu(menu);
        }

        if (this.schedules != null) {
            List<Schedule> openingHours = this.schedules.stream()
                    .map(scheduleDTO -> {
                        try {
                            // Parse day of the week and time from strings in DTO
                            DayOfWeek dayOfWeek = DayOfWeek.valueOf(scheduleDTO.getDay());
                            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;
                            LocalTime start = LocalTime.parse(scheduleDTO.getStartTime(), formatter);
                            LocalTime end = LocalTime.parse(scheduleDTO.getEndTime(), formatter);

                            // Use Schedule constructor
                            return new Schedule(dayOfWeek, start, end, scheduleDTO.getNumberOfWorkingStaff());
                        } catch (InvalidScheduleException e) {
                            System.err.println("Invalid data for schedule: " + e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            restaurant.setOpeningHours(openingHours);
        }



        return restaurant;
    }


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public List<MenuItem> getMenu() {
        return menu;
    }

    public List<ScheduleDTO> getSchedules() {
        return schedules;
    }

    public List<DiscountStrategy> getDiscountStrategies() {
        return discountStrategies;
    }

    public static class ScheduleDTO {
        private String id;
        private String day;
        private String startTime;
        private String endTime;
        private int numberOfWorkingStaff;
        private int currentLoad;
        private int capacityOfProduction;

        public ScheduleDTO(Schedule schedule) {
            this.id = schedule.getId() != null ? schedule.getId() : "";
            this.day = schedule.getDay() != null ? schedule.getDay().toString() : "Unknown Day";
            this.startTime = schedule.getStartTime() != null ? schedule.getStartTime().format(DateTimeFormatter.ISO_LOCAL_TIME) : "00:00:00";
            this.endTime = schedule.getEndTime() != null ? schedule.getEndTime().format(DateTimeFormatter.ISO_LOCAL_TIME) : "00:00:00";
            this.numberOfWorkingStaff = schedule.getNumberOfWorkingStaff();
            this.currentLoad = schedule.getCurrentLoad();
            this.capacityOfProduction = schedule.getCapacityOfProduction();
        }

        public ScheduleDTO(){

        }

        public String getId() {
            return id;
        }

        public String getDay() {
            return day;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public int getNumberOfWorkingStaff() {
            return numberOfWorkingStaff;
        }

        public int getCurrentLoad() {
            return currentLoad;
        }

        public int getCapacityOfProduction() {
            return capacityOfProduction;
        }


    }
}
