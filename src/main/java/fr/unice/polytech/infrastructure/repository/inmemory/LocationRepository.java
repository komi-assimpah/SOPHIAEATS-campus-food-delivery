package fr.unice.polytech.infrastructure.repository.inmemory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.unice.polytech.application.port.ILocationRepository;
import fr.unice.polytech.domain.models.Address;
import fr.unice.polytech.domain.models.delivery.DeliveryLocation;

public class LocationRepository implements ILocationRepository {
    private final Map<String, DeliveryLocation> locations;

    public LocationRepository() {
        this.locations = new HashMap<>();
        // Initialize with dummy data
        initLocations();
    }

    private void initLocations() {
        Address templier = new Address("930 Route des Colles,06410,Biot,France");
        locations.put("1", new DeliveryLocation("1", "Templiers A", templier));
        locations.put("2", new DeliveryLocation("2", "Templiers B", templier));
    }

    @Override
    public Optional<DeliveryLocation> findById(String id) {
        return locations.values().stream()
                .filter(location -> location.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<DeliveryLocation> findAll() {
        return locations.values().stream().toList();
    }

    @Override
    public DeliveryLocation add(DeliveryLocation entity) {
        return locations.put(entity.getId(), entity);
    }

    @Override
    public void update(DeliveryLocation entity) {
        locations.put(entity.getId(), entity);
    }

    @Override
    public void remove(DeliveryLocation entity) {
        locations.remove(entity.getId());
    }

    @Override
    public Optional<DeliveryLocation> findByName(String locationName) {
        return locations.values().stream()
                .filter(location -> location.getName().equals(locationName))
                .findFirst();
    }

    @Override
    public List<DeliveryLocation> findsByName(String locationName) {
        String searchName = locationName.toLowerCase();
        return locations.values().stream()
                .filter(r -> r.getName().toLowerCase().contains(searchName))
                .toList();
    }



    @Override
    public Optional<DeliveryLocation> findByNameAndAddress(String deliveryLocationName, Address address) {
        return locations.values().stream()
                .filter(location -> location.getName().equals(deliveryLocationName) && location.getAddress().equals(address))
                .findFirst();
    }

}