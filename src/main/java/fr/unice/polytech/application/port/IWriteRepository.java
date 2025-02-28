package fr.unice.polytech.application.port;

public interface IWriteRepository<T> {
    T add(T entity);
    void update(T entity);
    void remove(T entity);
}
