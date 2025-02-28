package fr.unice.polytech.infrastructure.repository.firebase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.*;
import fr.unice.polytech.application.dto.OrderDTO;
import fr.unice.polytech.application.dto.RestaurantDTO;
import fr.unice.polytech.application.port.IRestaurantRepository;
import fr.unice.polytech.domain.exceptions.InvalidScheduleException;
import fr.unice.polytech.domain.models.Address;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;

import fr.unice.polytech.domain.models.restaurant.Schedule;
import fr.unice.polytech.server.JaxsonUtils;

import java.awt.*;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class RestaurantRepository implements IRestaurantRepository {
    private static final String RESTAURANTS_NODE = "restaurants";

    private final DatabaseReference databaseReference;

    public RestaurantRepository() {
        try {
            this.databaseReference = FirebaseDb.getInstanceDB().getReference(RESTAURANTS_NODE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Firebase database reference", e);
        }
    }

    @Override
    public List<Restaurant> findByName(String restaurantName) {
        CountDownLatch latch = new CountDownLatch(1);
        List<Restaurant> restaurants = new ArrayList<>();

        databaseReference.orderByChild("name").equalTo(restaurantName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Restaurant restaurant = snapshot.getValue(Restaurant.class);
                            if (restaurant != null) {
                                restaurants.add(restaurant);
                            }
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        latch.countDown();
                    }
                });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return restaurants;
    }

    @Override
    public List<Restaurant> findsByName(String restaurantName) {

        return findByName(restaurantName);
    }

    @Override
    public Optional<Restaurant> findByNameAndAddress(String restaurantName, Address address) {
        return findAll().stream()
                .filter(restaurant -> restaurant.getName().equals(restaurantName)
                        && restaurant.getAddress().equals(address))
                .findFirst();
    }

    @Override
    public Optional<Restaurant> findById(String id) {
        CountDownLatch latch = new CountDownLatch(1);
        final Optional<Restaurant>[] result = new Optional[]{Optional.empty()};

        databaseReference.child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            try {
                                RestaurantDTO restaurantDTO = dataSnapshot.getValue(RestaurantDTO.class);
                                if (restaurantDTO != null) {
                                    Restaurant restaurant = restaurantDTO.toRestaurant();

                                    result[0] = Optional.of(restaurant);
                                } else {
                                    System.out.println("Failed to parse RestaurantDTO, data might be null or malformed");
                                }
                            } catch (Exception e) {
                                System.err.println("Error processing data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("DataSnapshot does not exist for ID: " + id);
                        }
                        latch.countDown();
                        System.out.println("Data processing complete, latch countdown done.");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.err.println("Firebase operation cancelled, error: " + databaseError.getMessage());
                        latch.countDown();
                    }
                });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return result[0];
    }


    @Override
    public List<Restaurant> findAll() {

        List<Restaurant> restaurants = new ArrayList<>();
        CountDownLatch doneSignal = new CountDownLatch(1);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        // Récupérer directement le DTO depuis Firebase
                        RestaurantDTO restaurantDTO = snapshot.getValue(RestaurantDTO.class);

                        if (restaurantDTO != null) {

                            // Convertir RestaurantDTO en Restaurant
                            Restaurant restaurant = restaurantDTO.toRestaurant();
                            restaurants.add(restaurant);

                        } else {
                            System.err.println("Aucun restaurant trouvé dans le snapshot: " + snapshot.getKey());
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur de désérialisation du snapshot: " + snapshot.getKey());
                        e.printStackTrace();
                    }
                }
                doneSignal.countDown(); // Assurez-vous que le signal de terminaison est appelé
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Erreur lors de la récupération des restaurants: " + databaseError.getMessage());
                doneSignal.countDown(); // Assurez-vous que le signal de terminaison est toujours appelé
            }
        });

        try {
            doneSignal.await(); // Attend que la récupération de tous les restaurants soit terminée
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while retrieving restaurants", e);
        }

        return restaurants;
    }




    @Override
    public Restaurant add(Restaurant entity) {
        if (entity.getId() == null || entity.getId().isEmpty()) {
            throw new IllegalArgumentException("Restaurant ID must be provided.");
        }
        RestaurantDTO restaurantDTO = new RestaurantDTO(entity);

        CountDownLatch latch = new CountDownLatch(1);
        databaseReference.child(entity.getId())
                .setValue(restaurantDTO, (databaseError, databaseReference) -> {
                    if (databaseError != null) {
                        System.err.println("Error adding restaurant: " + databaseError.getMessage());
                    } else {
                        System.out.println("Restaurant added successfully with ID: " + entity.getId());
                    }
                    latch.countDown();
                });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while adding restaurant", e);
        }
        return entity;
    }



    @Override
    public void update(Restaurant entity) {
        try {
            RestaurantDTO restaurantDTO = new RestaurantDTO(entity);
            databaseReference.child(entity.getId())
                    .setValue(restaurantDTO, (databaseError, databaseReference) -> {
                        if (databaseError != null) {
                            System.err.println("Firebase Error updating Restaurant: " + databaseError.getMessage());
                            System.err.println("Error Details: " + databaseError.getDetails());
                        } else {
                            System.out.println("Restaurant updated successfully with ID: " + entity.getId());
                        }
                    });
        } catch (Exception e) {
            System.err.println("Unexpected Error while updating Restaurant: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error updating Restaurant: " + e.getMessage(), e);
        }
    }

    @Override
    public void remove(Restaurant entity) {
        databaseReference.child(entity.getId()).removeValueAsync();
    }

    public static Restaurant dtoToRestaurant(RestaurantDTO restaurantDTO) {
        if (restaurantDTO == null) {
            return null;
        }

        Address address = new Address(
                restaurantDTO.getAddress().getStreet(),
                restaurantDTO.getAddress().getCity(),
                restaurantDTO.getAddress().getZipCode(),
                restaurantDTO.getAddress().getCountry()
        );


        Restaurant restaurant = new Restaurant(restaurantDTO.getId(), restaurantDTO.getName(), address);

        if (restaurantDTO.getMenu() != null ) {
            restaurantDTO.getMenu().forEach(menuItemDTO -> {
                MenuItem menuItem = new MenuItem(
                        menuItemDTO.getName(),
                        menuItemDTO.getPrice(),
                        menuItemDTO.getPreparationTimeInMinutes()
                );
                restaurant.getMenu().add(menuItem);
            });
        } else {
            System.out.println("No menu items found in DTO.");
        }


        if (restaurantDTO.getSchedules() != null ) {
            restaurantDTO.getSchedules().forEach(scheduleDTO -> {
                try {
                    Schedule schedule = dtoToSchedule(scheduleDTO);
                    restaurant.getOpeningHours().add(schedule);
                } catch (InvalidScheduleException e) {
                    System.err.println("Invalid schedule data: " + e.getMessage());
                }
            });
        } else {
            System.out.println("No schedules found in DTO.");
        }

        return restaurant;
    }



    private static Schedule dtoToSchedule(RestaurantDTO.ScheduleDTO scheduleDTO) throws InvalidScheduleException {
        if (scheduleDTO == null) {
            return null;
        }
        DayOfWeek day = DayOfWeek.valueOf(scheduleDTO.getDay());
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;
        LocalTime startTime = LocalTime.parse(scheduleDTO.getStartTime(), formatter);
        LocalTime endTime = LocalTime.parse(scheduleDTO.getEndTime(), formatter);
        int staff = scheduleDTO.getNumberOfWorkingStaff();
        return new Schedule(day, startTime, endTime, staff);
    }





}
