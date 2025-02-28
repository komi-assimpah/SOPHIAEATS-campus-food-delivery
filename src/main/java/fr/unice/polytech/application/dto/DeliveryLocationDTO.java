package fr.unice.polytech.application.dto;

public record DeliveryLocationDTO(String name, AddressDTO address) {
    public DeliveryLocationDTO {
        if (name == null || address == null) {
            throw new IllegalArgumentException("All fields are required.");
        }
    }
}
