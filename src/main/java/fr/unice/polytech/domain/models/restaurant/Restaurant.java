package fr.unice.polytech.domain.models.restaurant;


import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.unice.polytech.domain.exceptions.DiscountStrategyAlreadyExistsException;
import fr.unice.polytech.domain.exceptions.MenuItemNotFoundException;
import fr.unice.polytech.domain.models.Address;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.restaurant.discountstrategy.DiscountStrategy;
import fr.unice.polytech.domain.models.restaurant.discountstrategy.PercentageDiscount;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


public class Restaurant {
    private final String id;
    private String name;
    private Address address;
    private List<Schedule> openingHours = new ArrayList<>();
    private List<MenuItem> menu = new ArrayList<>();
    private List<DiscountStrategy> discountStrategies = new ArrayList<>();
    private final int AVERAGE_MEAL_PREP_TIME = 30; // in minutes


    // TODO: Refactor Constructors using Builder pattern (id and name, and adress are required, the rest are optional)
    public Restaurant(String id, String name, Address address){
        validateInputs(id, name, address);
        this.id = id;
        this.name = name;
        this.address = address;
        this.openingHours = new ArrayList<>();
        this.menu = new ArrayList<>();
        this.discountStrategies = new ArrayList<>();
    }

    public  Restaurant(){
        this.id = null ;
        this.openingHours = new ArrayList<>(); // Initialisation dans le constructeur


    }


    @JsonIgnore
    // Method to get a copy of the restaurant
    public Restaurant getCopy() {
        Restaurant restaurant = new Restaurant(this.id, this.name, this.address);
        restaurant.setMenu(this.menu);
        restaurant.setOpeningHours(new ArrayList<>(this.openingHours));
        restaurant.discountStrategies = new ArrayList<>(this.discountStrategies);
        return restaurant;
    }

//    public Restaurant(String id, String name, List<Schedule> openingHours, List<MenuItem> menu, int currentCapacity) {
//        this.id = id;
//        this.name = name;
//        this.openingHours = openingHours;
//        this.menu = menu;
//    }


    private void validateInputs(String id, String name, Address address) throws IllegalArgumentException {
        validateId(id);
        validateName(name);
        validateAddress(address);
    }

    private void validateId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
    }

    private void validateName(String restaurantName) {
        if (restaurantName == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
    }

    private void validateAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
    }


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String restaurantName) {
        validateName(restaurantName);
        this.name = restaurantName;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        validateAddress(address);
        this.address = address;
    }


    public List<MenuItem> getMenu() {
        return menu;
    }


    public void setMenu(List<MenuItem> menu) {
        this.menu = menu;
    }

    public void setOpeningHours(List<Schedule> openingHours) {
        this.openingHours = openingHours;
    }

    public void addDiscountStrategy(DiscountStrategy discountStrategy) throws DiscountStrategyAlreadyExistsException {
        if (discountStrategies.contains(discountStrategy)) {
            throw new DiscountStrategyAlreadyExistsException("Discount strategy already exists");
        }
        this.discountStrategies.add(discountStrategy);
    }

    public void applyDiscountStrategy(Order order) {
        if (this.discountStrategies.isEmpty()) {
            this.discountStrategies.add(new PercentageDiscount(10, 10));
        }
        for (DiscountStrategy strategy : discountStrategies) {
                strategy.applyDiscount(order);
        }
    }

    public void applyGroupPercentageDiscount(Order order, int totalQuantity) {
        for (DiscountStrategy strategy : discountStrategies) {
            if (order.isSubOrder() && strategy instanceof PercentageDiscount) {
                ((PercentageDiscount) strategy).applyGroupDiscount(order, totalQuantity);
            }
        }
    }

    public void addMenuItem(MenuItem item) {
        // TODO: What if the item that already exists has a different price or preparation time?
        // TODO: TBD about the identifier of the menu item, for now the name is the unique identifier
        menu.stream()
                .filter(menuItem -> menuItem.getName().equals(item.getName()))
                .findFirst()
                .ifPresentOrElse(
                        menuItem -> {
                            throw new IllegalArgumentException("Menu item '" + item.getName() + "' already exists. Please update the existing menu item instead or use another name.");
                        },
                        () -> menu.add(item)
                );
    }

    public void removeMenuItem(String name) throws MenuItemNotFoundException {
        if (!menu.removeIf(item -> item.getName().equals(name))) {
            throw new MenuItemNotFoundException("Menu item '" + name + "' not found");
        }
    }

//    public List<MenuItem> getMenuItemsAvailableByDate(LocalTime orderDeliveryDate) {
//        List<MenuItem> availableItems = new ArrayList<>();
//        for (MenuItem item : menu) {
//            if (item.isAvailableByDate(orderDeliveryDate)) {
//                availableItems.add(item);
//            }
//        }
//        return availableItems;
//    }

    public MenuItem getMenuItemById(String menuItemId) throws MenuItemNotFoundException {
        System.out.println("Trying to get the menu by id");
        System.out.println(menu);
        return menu.stream()
                .filter(item -> item.getId().equals(menuItemId))
                .findFirst()
                .orElseGet(() -> {
                    System.out.println("Menu item with ID '" + menuItemId + "' not found");
                    return null;
                });

    }


    // Schedule
    public void addSchedule(Schedule schedule) {
        openingHours.add(schedule);
        // Keep the schedules sorted by day then by start time
        openingHours.sort(Comparator.comparing(Schedule::getDay).thenComparing(Schedule::getStartTime));
        mergeAdjacentSchedules();
    }

    private void mergeAdjacentSchedules() {
        if (openingHours.isEmpty()) { return; }

        List<Schedule> mergedSchedules = new ArrayList<>();
        // Start with the first schedule
        Schedule lastSchedule = openingHours.getFirst();
        for (int i = 1; i < openingHours.size(); i++) {
            Schedule currentSchedule = openingHours.get(i);

            if (lastSchedule.getEndTime().equals(currentSchedule.getStartTime()) &&
                    lastSchedule.getNumberOfWorkingStaff() == currentSchedule.getNumberOfWorkingStaff()) {
                lastSchedule = new Schedule(
                        lastSchedule.getDay(),
                        lastSchedule.getStartTime(),
                        currentSchedule.getEndTime(),
                        lastSchedule.getNumberOfWorkingStaff()
                );
            } else {
                mergedSchedules.add(lastSchedule);
                lastSchedule = currentSchedule;
            }
        }
        mergedSchedules.add(lastSchedule);
        openingHours.clear();
        openingHours.addAll(mergedSchedules);
    }

    public List<Schedule> getSchedules() {
        return openingHours ;
    }



    public Schedule getScheduleById(String scheduleId) {
        return openingHours.stream()
                .filter(schedule -> schedule.getId().equals(scheduleId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No schedule found with the given ID"));
    }

    public void updateSchedule(Schedule existingSchedule, Schedule updatedSchedule) {
        openingHours.remove(existingSchedule);
        openingHours.add(updatedSchedule);
        openingHours.sort(Comparator.comparing(Schedule::getDay).thenComparing(Schedule::getStartTime));
        mergeAdjacentSchedules();
    }

    public void removeSchedule(Schedule schedule) {
        openingHours.remove(schedule);
    }

    public int getCurrentCapacity(Schedule schedule) {
        return schedule.getCapacityOfProduction();
    }

    // Validate if the restaurant is open for a given time
    public boolean isOpenAt(LocalDateTime dateTime) {
        return openingHours.stream().anyMatch(schedule -> schedule.isWithinOpeningHours(dateTime));
    }


//TODO: Refactor this method is already implemented in the Schedule class, and is useless like it's right now
//    public boolean canPrepareItemAt(MenuItem item, LocalDateTime deliveryTime) {
//        Duration preparationTime = Duration.ofSeconds(item.getPreparationTimeInMinutes());
//        Duration timeUntilDelivery = Duration.between(LocalDateTime.now(), deliveryTime);
//        return preparationTime.compareTo(timeUntilDelivery) <= 0;
//    }


    public Schedule getScheduleByDay(LocalDateTime time) {
        LocalTime localTime = LocalTime.of(time.getHour(), time.getMinute());
        return openingHours.stream()
                .filter(schedule -> schedule.getDay().equals(time.getDayOfWeek()))
                .filter(schedule -> schedule.isWithinTimeSlot(time.toLocalTime()))
                .min(Comparator.comparing(schedule -> Duration.between(schedule.getStartTime(), time.toLocalTime()).abs()))
                .orElseThrow(() -> new IllegalArgumentException("No schedule found for the given date"));
    }

    public List<DiscountStrategy> getDiscountStrategies() {
        return this.discountStrategies;
    }

    public void clearDiscountStrategies() {
        this.discountStrategies.clear();
    }

    public Optional<MenuItem> getMenuItemByName(String menuItemName) {
        return menu.stream()
                .filter(item -> item.getName().equals(menuItemName))
                .findFirst();
    }

    public List<Schedule> getOpeningHours() {
        return openingHours ;
    }
}