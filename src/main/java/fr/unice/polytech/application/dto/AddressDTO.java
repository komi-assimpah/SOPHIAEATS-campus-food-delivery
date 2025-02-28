package fr.unice.polytech.application.dto;

public record AddressDTO(String street, String zipCode, String city, String country) {
    public AddressDTO {
        if (street == null || zipCode == null || city == null || country == null) {
            throw new IllegalArgumentException("All fields are required.");
        }
    }
}
