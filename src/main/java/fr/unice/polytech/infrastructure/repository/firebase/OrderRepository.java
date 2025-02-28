package fr.unice.polytech.infrastructure.repository.firebase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.*;
import fr.unice.polytech.application.dto.OrderDTO;
import fr.unice.polytech.application.port.IOrderRepository;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.order.OrderItem;

import java.io.IOException;
import java.sql.SQLOutput;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class OrderRepository implements IOrderRepository {
    private static final String ORDERS_NODE = "orders";
    private final DatabaseReference databaseReference;
    public OrderRepository() {
        try {
            this.databaseReference = FirebaseDb.getInstanceDB().getReference(ORDERS_NODE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Firebase database reference", e);
        }
    }






    @Override
    public Order add(Order entity) {
        try {
            OrderDTO orderDTO = new OrderDTO(entity);
            ObjectMapper mapper = new ObjectMapper();
            String jsonEntity = mapper.writeValueAsString(orderDTO);
            databaseReference.child(entity.getId())
                    .setValue(orderDTO, (databaseError, databaseReference) -> {
                        if (databaseError != null) {
                            System.err.println("Error Details: " + databaseError.getDetails());
                        } else {
                            System.out.println("Order added successfully with ID: " + entity.getId());
                        }
                    });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error adding Order: " + e.getMessage(), e);
        }
        return entity;
    }

    @Override
    public Optional<Order> findById(String id) {
        CountDownLatch latch = new CountDownLatch(1);
        final Optional<Order>[] result = new Optional[]{Optional.empty()};
        databaseReference.child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            try {
                                OrderDTO orderDTO = dataSnapshot.getValue(OrderDTO.class);
                                if (orderDTO != null) {
                                    Order order = dtoToOrder(orderDTO);
                                    result[0] = Optional.of(order);
                                } else {
                                    System.out.println("Failed to parse OrderDTO, data might be null or malformed");
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
    public List<Order> findAll() {
        CountDownLatch latch = new CountDownLatch(1);
        List<Order> orders = new ArrayList<>();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    OrderDTO orderDTO = snapshot.getValue(OrderDTO.class);
                    if (orderDTO != null) {
                        orders.add(dtoToOrder(orderDTO));
                    }
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Firebase operation cancelled during findAll, error: " + databaseError.getMessage());
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrupted while waiting for findAll to complete.");
        }

        return orders;
    }

    @Override
    public void update(Order entity) {
        try {
            OrderDTO orderDTO = new OrderDTO(entity);
            databaseReference.child(entity.getId())
                    .setValue(orderDTO, (databaseError, databaseReference) -> {
                        if (databaseError != null) {
                            System.err.println("Firebase Error updating Order: " + databaseError.getMessage());
                            System.err.println("Error Details: " + databaseError.getDetails());
                        } else {
                            System.out.println("Order updated successfully with ID: " + entity.getId());
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating Order: " + e.getMessage(), e);
        }
    }

    @Override
    public void remove(Order entity) {
        databaseReference.child(entity.getId())
                .removeValue((databaseError, databaseReference) -> {
                    if (databaseError != null) {
                        System.err.println("Firebase Error removing Order: " + databaseError.getMessage());
                        System.err.println("Error Details: " + databaseError.getDetails());
                    } else {
                        System.out.println("Order removed successfully with ID: " + entity.getId());
                    }
                });
    }


    private Order dtoToOrder(OrderDTO dto) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime deliveryTime = (dto.getDeliveryTime() != null)
                ? LocalDateTime.parse(dto.getDeliveryTime(), formatter)
                : LocalDateTime.MIN;
        LocalDateTime orderTime = (dto.getOrderTime() != null)
                ? LocalDateTime.parse(dto.getOrderTime(), formatter)
                : LocalDateTime.MIN;
        List<OrderItem> orderItems = (dto.getOrderItems() != null) ? dto.getOrderItems() : Collections.emptyList();
        return new Order.OrderBuilder(dto.getId(), dto.getUser(), dto.getRestaurantDTO().toRestaurant(), dto.getDeliveryLocation(), deliveryTime, orderTime)
                .setOrderItems(orderItems)
                .setAsSubOrder(dto.getId())
                .setStatus(dto.getStatus())
                .build();
    }

}
