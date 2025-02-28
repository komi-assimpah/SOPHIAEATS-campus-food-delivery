package fr.unice.polytech.application.exceptions;

public class EntityAlreadyExist extends ApplicationException {
    public EntityAlreadyExist(String entityType, Object identifier) {
        super(entityType + " with id " + identifier + " not found");
    }

    public EntityAlreadyExist(String entityType, Object identifier, Throwable cause) {
        super(entityType + " with id " + identifier + " not found", cause);
    }

    public EntityAlreadyExist(String message) {
        super(message);
    }
}
