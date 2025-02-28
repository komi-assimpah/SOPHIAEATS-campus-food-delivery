package fr.unice.polytech.junit.domain.models.delivery;

import fr.unice.polytech.domain.models.Address;
import fr.unice.polytech.domain.models.delivery.DeliveryLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class DeliveryLocationTest {

    private DeliveryLocation deliveryLocation;
    private Address address;

    @BeforeEach
    void setUp() {
        address = new Address("930 Route des Colles", "06410", "Biot", "France");
        deliveryLocation = new DeliveryLocation("amphi Forum", address);
    }

    @Test
    void testDeliveryLocationInitializationWithId() {
        DeliveryLocation locationWithId = new DeliveryLocation("123", "Main Library", address);

        assertEquals("123", locationWithId.getId());
        assertEquals("Main Library", locationWithId.getName());
        assertEquals(address, locationWithId.getAddress());
    }

    @Test
    void testDeliveryLocationInitializationWithoutId() {
        assertNotNull(deliveryLocation.getId());
        assertEquals("amphi Forum", deliveryLocation.getName());
        assertEquals(address, deliveryLocation.getAddress());
    }

    @Test
    void testEqualsAndHashCodeWithSameId() {
        DeliveryLocation locationWithSameId = new DeliveryLocation(deliveryLocation.getId(), "Different Location", new Address("other street", "06410", "Biot", "France"));

        assertEquals(deliveryLocation, locationWithSameId);
        assertEquals(deliveryLocation.hashCode(), locationWithSameId.hashCode());
    }

    @Test
    void testEqualsAndHashCodeWithDifferentId() {
        DeliveryLocation locationWithDifferentId = new DeliveryLocation("differentId", "amphi Forum", address);

        assertNotEquals(deliveryLocation, locationWithDifferentId);
        assertNotEquals(deliveryLocation.hashCode(), locationWithDifferentId.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "DeliveryLocation{id='" + deliveryLocation.getId() + "', name='amphi Forum', address=" + address + "}";
        assertEquals(expectedString, deliveryLocation.toString());
    }
}