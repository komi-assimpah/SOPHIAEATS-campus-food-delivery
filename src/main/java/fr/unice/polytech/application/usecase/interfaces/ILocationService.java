package fr.unice.polytech.application.usecase.interfaces;

import java.util.List;
import java.util.Optional;

import fr.unice.polytech.application.dto.DeliveryLocationDTO;
import fr.unice.polytech.application.exceptions.EntityAlreadyExist;
import fr.unice.polytech.application.port.ILocationRepository;
import fr.unice.polytech.domain.models.delivery.DeliveryLocation;

public interface ILocationService {
    List<DeliveryLocation> getDeliveryLocations();

    Optional<DeliveryLocation> getLocationById(String locationId);

    Optional<DeliveryLocation> getLocationByName(String locationName);

    List<DeliveryLocation> getLocationsByName(String locationName);

    DeliveryLocation addLocation(DeliveryLocationDTO deliveryLocation) throws EntityAlreadyExist;

    void setLocationRepository(ILocationRepository locationRepository);
}
