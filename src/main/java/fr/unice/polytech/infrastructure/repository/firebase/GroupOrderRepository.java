package fr.unice.polytech.infrastructure.repository.firebase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.*;
import fr.unice.polytech.application.dto.GroupOrderDTO;
import fr.unice.polytech.application.port.IGroupOrderRepository;
import fr.unice.polytech.domain.models.groupOrder.GroupOrder;
import fr.unice.polytech.server.JaxsonUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class GroupOrderRepository implements IGroupOrderRepository {

    private static final String GROUP_ORDERS_NODE = "groupOrders";

    private final DatabaseReference databaseReference;

    public GroupOrderRepository() {
        try {
            this.databaseReference = FirebaseDb.getInstanceDB().getReference(GROUP_ORDERS_NODE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Firebase database reference", e);
        }
    }

    public static GroupOrderRepository getInstance() {
        return new GroupOrderRepository();
    }


    @Override
    public Optional<GroupOrder> findById(String id) {
        CountDownLatch latch = new CountDownLatch(1);
        final Optional<GroupOrder>[] result = new Optional[]{Optional.empty()};

        databaseReference.child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            try {
                                GroupOrderDTO groupOrderDTO = dataSnapshot.getValue(GroupOrderDTO.class);
                                if (groupOrderDTO != null) {
                                    GroupOrder groupOrder = dtoToGroupOrder(groupOrderDTO);
                                    result[0] = Optional.of(groupOrder);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
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

        return result[0];
    }

    @Override
    public List<GroupOrder> findAll() {
        CountDownLatch doneSignal = new CountDownLatch(1);
        List<GroupOrder> groupOrders = new ArrayList<>();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        String json = new ObjectMapper().writeValueAsString(childSnapshot.getValue());
                        GroupOrder groupOrder = JaxsonUtils.fromJson(json, GroupOrder.class);
                        groupOrders.add(groupOrder);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                doneSignal.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                doneSignal.countDown();
            }
        });

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return groupOrders;
    }

    @Override
    public GroupOrder add(GroupOrder entity) {
        GroupOrderDTO groupOrderDTO = null;
        try {
            // Conversion de GroupOrder en GroupOrderDTO
            groupOrderDTO = groupOrderToDto(entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw e; // Lancer l'exception après journalisation
        }

        try {
            // Ajout de l'objet dans la base de données
            databaseReference.child(entity.getGroupID()).setValueAsync(groupOrderDTO)
                    .addListener(() -> System.out.println("Ajout réussi de GroupOrder avec ID : " + entity.getGroupID()),
                            e -> {
                                System.out.println(e);
                            });
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return entity;
    }


    @Override
    public void update(GroupOrder entity) {

        GroupOrderDTO groupOrderDTO = null;
        try {
            groupOrderDTO = groupOrderToDto(entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la conversion de GroupOrder en DTO", e); // Lancer l'exception après journalisation
        }

        try {
            // Mise à jour de l'objet dans la base de données
            databaseReference.child(entity.getGroupID()).updateChildrenAsync(groupOrderDTO.toMap())
                    .addListener(() -> System.out.println(" Mise à jour réussie de GroupOrder avec ID : " + entity.getGroupID()),
                            e -> {
                                System.err.println("Erreur lors de la mise à jour de GroupOrder avec ID : " + entity.getGroupID());

                            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour de GroupOrder dans la base de données", e); // Lancer l'exception après journalisation
        }

    }

    @Override
    public void remove(GroupOrder entity) {
        databaseReference.child(entity.getGroupID()).removeValueAsync();
    }

    private GroupOrder dtoToGroupOrder(GroupOrderDTO groupOrderDTO) {
        GroupOrder groupOrder = new GroupOrder(
                groupOrderDTO.getDeliveryLocationID(),
                stringToLocalDateTime(groupOrderDTO.getDeliveryTime())
        );
        groupOrder.setConfirmationMoment(stringToLocalDateTime(groupOrderDTO.getConfirmationMoment()));
        groupOrder.setStatus(groupOrderDTO.getStatus());
        for (String subOrderID : groupOrderDTO.getOrderIDs()) {
            groupOrder.addSubOrder(subOrderID);
        }
        return groupOrder;
    }

    private GroupOrderDTO groupOrderToDto(GroupOrder groupOrder) {
        GroupOrderDTO groupOrderDTO = new GroupOrderDTO();
        groupOrderDTO.setGroupID(groupOrder.getGroupID());
        groupOrderDTO.setCreationMoment(localDateTimeToString(groupOrder.getCreationMoment()));
        groupOrderDTO.setConfirmationMoment(localDateTimeToString(groupOrder.getConfirmationMoment()));
        groupOrderDTO.setDeliveryLocationID(groupOrder.getDeliveryLocationID());
        groupOrderDTO.setDeliveryTime(localDateTimeToString(groupOrder.getDeliveryTime()));
        groupOrderDTO.setStatus(groupOrder.getStatus());
        groupOrderDTO.setOrderIDs(groupOrder.getSubOrderIDs());
        return groupOrderDTO;
    }

    private String localDateTimeToString(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }

    private LocalDateTime stringToLocalDateTime(String dateTimeString) {
        return dateTimeString != null && !dateTimeString.isEmpty() ? LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }
}
