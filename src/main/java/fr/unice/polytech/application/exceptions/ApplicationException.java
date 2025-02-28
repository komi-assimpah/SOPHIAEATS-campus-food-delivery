package fr.unice.polytech.application.exceptions;

/**
 * Base exception class for all application-specific exceptions.
 */
public class ApplicationException extends RuntimeException {

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}