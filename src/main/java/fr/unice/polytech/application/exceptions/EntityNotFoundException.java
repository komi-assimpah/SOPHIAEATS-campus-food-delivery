package fr.unice.polytech.application.exceptions;

public class EntityNotFoundException extends ApplicationException {

    public EntityNotFoundException(String entityType, Object identifier) {
        super(entityType + " with id " + identifier + " not found");
    }

    public EntityNotFoundException(String entityType, Object identifier, Throwable cause) {
        super(entityType + " with id " + identifier + " not found", cause);
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
