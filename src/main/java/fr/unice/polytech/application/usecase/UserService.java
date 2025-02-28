package fr.unice.polytech.application.usecase;

import java.util.List;
import java.util.Optional;

import fr.unice.polytech.application.dto.UserDTO;
import fr.unice.polytech.application.exceptions.EntityNotFoundException;
import fr.unice.polytech.application.port.IUserRepository;
import fr.unice.polytech.application.usecase.interfaces.IUserService;
import fr.unice.polytech.domain.models.user.User;
import org.mindrot.jbcrypt.BCrypt;


public class UserService implements IUserService {
    private  IUserRepository userRepository;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }


    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    @Override
    public Optional<User> getUserById(String userId) throws EntityNotFoundException {
        return userRepository.findById(userId);
    }

    @Override
    public void updateUser(User user) {
        userRepository.update(user);
    }

    @Override
    public User createUser(String name, String email, String password) {
        String hashedPassword = hashPassword(password);
        User user = new User(name, email, hashedPassword);

        userRepository.findAll().forEach(u -> {
            if (u.getEmail().equals(email)) {
                throw new IllegalArgumentException("Email already exists");
            }
        });

        return userRepository.add(user);
    }


    @Override
    public User authenticate(String email, String password) throws EntityNotFoundException {
        User userOpt = userRepository.findAll().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur", email));

        if (!verifyPassword(password, userOpt.getPassword())) {
            throw new EntityNotFoundException("Le mote de passe est incorrect");
        }

        return userOpt;
    }

}
