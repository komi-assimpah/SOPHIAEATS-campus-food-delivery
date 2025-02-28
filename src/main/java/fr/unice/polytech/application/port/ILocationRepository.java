package fr.unice.polytech.application.port;

import java.util.List;
import java.util.Optional;

import fr.unice.polytech.domain.models.Address;
import fr.unice.polytech.domain.models.delivery.DeliveryLocation;

public interface ILocationRepository extends IWriteRepository<DeliveryLocation>, IReadRepository<DeliveryLocation> {
    Optional<DeliveryLocation> findByName(String locationName);

    List<DeliveryLocation> findsByName(String locationName);

    // Using Domain Address record is not good practice, but for the sake of simplicity, we will use it here instead of creating a separate AddressDTOs
    Optional<DeliveryLocation> findByNameAndAddress(String deliveryLocationName, Address address);
}
