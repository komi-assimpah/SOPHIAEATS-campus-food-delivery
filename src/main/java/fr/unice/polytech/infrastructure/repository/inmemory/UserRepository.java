package fr.unice.polytech.infrastructure.repository.inmemory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.unice.polytech.application.port.IUserRepository;
import fr.unice.polytech.domain.models.payment.PaymentDetails;
import fr.unice.polytech.domain.models.user.User;
import fr.unice.polytech.domain.models.user.UserStatus;
import fr.unice.polytech.infrastructure.repository.exceptions.EntityAlreadyExistsException;

public class UserRepository implements IUserRepository {
    private final Map<String, User> users;

    public UserRepository() {
        this.users = new HashMap<>();
        initUsers();
    }

    private void initUsers() {
        User user1 = new User("1", "John Doe", "john@doe.com", "password");
        PaymentDetails paymentDetails = new PaymentDetails("1234 5678 9012 3456", "12/24", "123");
        user1.addPaymentMethod(paymentDetails);
        User user2 = new User("2", "Jane Doe", "jane@doe.com", "password");
        user2.addPaymentMethod(paymentDetails);
        user1.setType(UserStatus.CAMPUS_STUDENT);
        user2.setType(UserStatus.CAMPUS_STUDENT);
        users.put(user1.getId(), user1);
        users.put(user2.getId(), user2);
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return users.values().stream().toList();
    }

    @Override
    public User add(User entity) throws EntityAlreadyExistsException {
        if (users.containsKey(entity.getId())) {
            throw new EntityAlreadyExistsException("User with id " + entity.getId() + " already exists");
        }
        users.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void update(User entity) {
        users.put(entity.getId(), entity);
    }

    @Override
    public void remove(User entity) {
        users.remove(entity.getId());
    }

}