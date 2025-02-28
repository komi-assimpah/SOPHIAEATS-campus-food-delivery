package fr.unice.polytech.application.port;

import fr.unice.polytech.domain.models.user.User;


public interface IUserRepository extends IReadRepository<User>, IWriteRepository<User> {

    User add(User entity);

    //Optional<User> findByEmail(String email);

}
