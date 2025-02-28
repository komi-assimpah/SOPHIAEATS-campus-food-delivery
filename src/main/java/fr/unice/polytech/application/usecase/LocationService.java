package fr.unice.polytech.application.usecase;

import java.util.List;
import java.util.Optional;

import fr.unice.polytech.application.dto.DeliveryLocationDTO;
import fr.unice.polytech.application.exceptions.EntityAlreadyExist;
import fr.unice.polytech.application.port.ILocationRepository;
import fr.unice.polytech.application.usecase.interfaces.ILocationService;
import fr.unice.polytech.domain.models.Address;
import fr.unice.polytech.domain.models.delivery.DeliveryLocation;
import fr.unice.polytech.utils.Utils;

public class LocationService implements ILocationService {

    private ILocationRepository locationRepository;

    public LocationService(ILocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public List<DeliveryLocation> getDeliveryLocations() {
        return locationRepository.findAll();
    }

    @Override
    public Optional<DeliveryLocation> getLocationById(String locationId) {
        return locationRepository.findById(locationId);
    }

    @Override
    public Optional<DeliveryLocation> getLocationByName(String locationName) {
        return locationRepository.findByName(locationName);
    }

    //this version is more optimized for the server searching by name, the previous version has been used with plenty tests
    @Override
    public List<DeliveryLocation> getLocationsByName(String locationName) {
        return locationRepository.findsByName(locationName);
    }

    @Override
    public DeliveryLocation addLocation(DeliveryLocationDTO deliveryLocation) throws EntityAlreadyExist {
        Address address = new Address(deliveryLocation.address().street(), deliveryLocation.address().zipCode(), deliveryLocation.address().city(), deliveryLocation.address().country());

        // Check if a delivery location with the same name and address already exists
        Optional<DeliveryLocation> existingLocation = locationRepository.findByNameAndAddress(deliveryLocation.name(), address);
        if (existingLocation.isPresent()) {
            throw new EntityAlreadyExist("A location with the same name already exists");
        }
        String id = Utils.generateUniqueId();
        DeliveryLocation location = new DeliveryLocation(id, deliveryLocation.name(), address);
        locationRepository.add(location);
        return location;
    }

    public void setLocationRepository(ILocationRepository locationRepository){
        this.locationRepository = locationRepository ;
    }

}
