package fr.unice.polytech.infrastructure.repository.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import fr.unice.polytech.application.port.IUserRepository;
import fr.unice.polytech.domain.models.delivery.DeliveryLocation;
import fr.unice.polytech.domain.models.user.User;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class UserRepository implements IUserRepository {

        private static final String DELIVERY_USERS_NODE = "users";

        private final DatabaseReference databaseReference;

        public UserRepository() {
            try {
                this.databaseReference = FirebaseDb.getInstanceDB().getReference(DELIVERY_USERS_NODE);
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize Firebase database reference", e);
            }
        }

    @Override
    public Optional<User> findById(String id) {
        CountDownLatch latch = new CountDownLatch(1);
        final Optional<User>[] result = new Optional[]{Optional.empty()};

        databaseReference.child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            result[0] = Optional.of(user);
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
    public List<User> findAll() {
        return List.of();
    }

    @Override
    public User add(User entity) {
        databaseReference.child(entity.getId()).setValueAsync(entity);
        return entity;
    }

    @Override
    public void update(User entity) {

    }

    @Override
    public void remove(User entity) {

    }
}


