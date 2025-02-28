package fr.unice.polytech.application.port;

import java.util.List;
import java.util.Optional;

public interface IReadRepository<T> {
    Optional<T> findById(String id);
    List<T> findAll();
}
