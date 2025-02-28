package fr.unice.polytech.domain.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Address {
    private final String street;
    private final String zipCode;
    private final String city;
    private final String country;

    /**
     * Constructs an Address using individual fields.
     *
     * @param street  The street part of the address.
     * @param zipCode The postal code of the address.
     * @param city    The city part of the address.
     * @param country The country part of the address.
     */
    public Address(String street, String zipCode, String city, String country) {
        this.street = street;
        this.zipCode = zipCode;
        this.city = city;
        this.country = country;
    }

    /**
     * Constructs an Address from a comma-separated string containing
     * street, zip code, city, and country.
     *
     * @param fullAddress Comma-separated full address (street, zipCode, city, country).
     */
    public Address(String fullAddress) {
        String[] parts = fullAddress.split(",");
        this.street = parts.length > 0 ? parts[0] : null;
        this.zipCode = parts.length > 1 ? parts[1] : null;
        this.city = parts.length > 2 ? parts[2] : null;
        this.country = parts.length > 3 ? parts[3] : null;
    }

    public Address() {
        this.street = null;
        this.zipCode = null;
        this.city = null;
        this.country = null;
    }


    /**
     * Serializes the address into a Map object, suitable for storage in Firebase.
     *
     * @return A Map representation of the address.
     */

    // Getters
    public String getStreet() {
        return street;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(street, address.street) &&
                Objects.equals(zipCode, address.zipCode) &&
                Objects.equals(city, address.city) &&
                Objects.equals(country, address.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, zipCode, city, country);
    }

    // toString method for debugging and logging purposes
    @Override
    public String toString() {
        return "Address{" +
                "street='" + street + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
