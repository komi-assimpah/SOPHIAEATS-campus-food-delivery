package fr.unice.polytech.domain.exceptions;

public class DiscountStrategyAlreadyExistsException extends RuntimeException {
    public DiscountStrategyAlreadyExistsException(String message) {
        super(message);
    }
}
