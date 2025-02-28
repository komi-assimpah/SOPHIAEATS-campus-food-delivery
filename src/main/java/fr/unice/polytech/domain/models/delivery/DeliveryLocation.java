package fr.unice.polytech.domain.models.delivery;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.unice.polytech.domain.models.Address;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DeliveryLocation {

    private final String id;
    private final String name;
    private final Address address;

    @JsonCreator
    public DeliveryLocation(
            @JsonProperty("id") String id,
            @JsonProperty("name") String locationName,
            @JsonProperty("address") Address address
    ) {
        this.id = id;
        this.name = locationName;
        this.address = address;
    }



    public DeliveryLocation(String locationName, Address address) {
        this(UUID.randomUUID().toString(), locationName, address);
    }

    public DeliveryLocation() {
        this.id = null;
        this.name = null;
        this.address = null;
    }




    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryLocation that = (DeliveryLocation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "DeliveryLocation{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", address=" + address +
                '}';
    }

//    public Map<String, Object> serializeDeliveryLocationForFirebase() {
//        Map<String, Object> data = new HashMap<>();
//        data.put("id", id);
//        data.put("name", name);
//
//        if (address != null) {
//            data.put("address", address.serializeAddressForFirebase());
//        }
//
//        return data;
//    }
}
