package fr.unice.polytech.infrastructure.repository.firebase;

import com.google.firebase.database.*;
import fr.unice.polytech.application.port.ILocationRepository;
import fr.unice.polytech.domain.models.Address;
import fr.unice.polytech.domain.models.delivery.DeliveryLocation;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class LocationRepository implements ILocationRepository {
    private static final String DELIVERY_LOCATIONS_NODE = "locations";

    private final DatabaseReference databaseReference;

    public LocationRepository() {
        try {
            this.databaseReference = FirebaseDb.getInstanceDB().getReference(DELIVERY_LOCATIONS_NODE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Firebase database reference", e);
        }
    }

    @Override
    public Optional<DeliveryLocation> findByName(String locationName) {
        CountDownLatch latch = new CountDownLatch(1);
        final Optional<DeliveryLocation>[] result = new Optional[]{Optional.empty()};

        databaseReference.orderByChild("name").equalTo(locationName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            DeliveryLocation location = snapshot.getValue(DeliveryLocation.class);
                            if (location != null) {
                                result[0] = Optional.of(location);
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
    public List<DeliveryLocation> findsByName(String locationName) {
        return List.of();
    }

    @Override
    public Optional<DeliveryLocation> findByNameAndAddress(String deliveryLocationName, Address address) {
        return findAll().stream()
                .filter(location -> location.getName().equals(deliveryLocationName)
                        && location.getAddress().equals(address))
                .findFirst();
    }

    @Override
    public Optional<DeliveryLocation> findById(String id) {
        CountDownLatch latch = new CountDownLatch(1);
        final Optional<DeliveryLocation>[] result = new Optional[]{Optional.empty()};

        databaseReference.child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DeliveryLocation location = dataSnapshot.getValue(DeliveryLocation.class);
                        if (location != null) {
                            result[0] = Optional.of(location);
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
    public List<DeliveryLocation> findAll() {
        CountDownLatch latch = new CountDownLatch(1);
        List<DeliveryLocation> locations = new ArrayList<>();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    DeliveryLocation location = snapshot.getValue(DeliveryLocation.class);
                    if (location != null) {
                        locations.add(location);
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

        return locations;
    }

    @Override
    public DeliveryLocation add(DeliveryLocation entity) {
        databaseReference.child(entity.getId()).setValueAsync(entity);
        return entity;
    }

    @Override
    public void update(DeliveryLocation entity) {
        databaseReference.child(entity.getId()).setValueAsync(entity);
    }

    @Override
    public void remove(DeliveryLocation entity) {
        databaseReference.child(entity.getId()).removeValueAsync();
    }
}
