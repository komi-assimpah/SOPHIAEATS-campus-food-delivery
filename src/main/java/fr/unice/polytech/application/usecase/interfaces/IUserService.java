package fr.unice.polytech.application.usecase.interfaces;

import java.util.List;
import java.util.Optional;


import fr.unice.polytech.application.exceptions.EntityNotFoundException;
import fr.unice.polytech.domain.models.user.User;

public interface IUserService {
    Optional<User> getUserById(String userId);

    void updateUser(User user);

    User createUser(String name, String email, String password);


    User authenticate(String email, String password) throws EntityNotFoundException;

}
