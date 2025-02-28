package fr.unice.polytech.stepDefs.groupOrder;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import fr.unice.polytech.Facade;
import fr.unice.polytech.application.dto.AddressDTO;
import fr.unice.polytech.application.dto.DeliveryLocationDTO;
import fr.unice.polytech.application.usecase.interfaces.ILocationService;
import fr.unice.polytech.domain.models.groupOrder.GroupOrder;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CreateGroupOrderStepdefs {

    private GroupOrder groupOrder;
    private String deliveryLocationID;
    private LocalDateTime deliveryTime;
    private final String userID;
    private final ILocationService locationService;
    private final Facade facade;
    private Exception exception;

    public CreateGroupOrderStepdefs() {
        this.facade = new Facade();
        this.locationService = this.facade.getLocationService();

        this.userID = this.facade.createUser("John Doe", "johndoe@gmail.com", "password").getId();
    }

    @Given("the list of delivery locations:")
    public void theListOfDeliveryLocations(List<Map<String, String>> deliveryLocations) {
        for (Map<String, String> location : deliveryLocations) {
            try {
                String locationName = location.get("locationName");
                String street = location.get("street");
                String zipCode = location.get("zipCode");
                String city = location.get("city");
                String country = location.get("country");

                AddressDTO addressDTO = new AddressDTO(street, zipCode, city, country);
                DeliveryLocationDTO deliveryLocationDTO = new DeliveryLocationDTO(locationName, addressDTO);
                this.locationService.addLocation(deliveryLocationDTO);
            } catch (Exception e) {
                Logger.getGlobal().warning(e.getMessage());
            }
        }
    }

    @When("a campus user selects {string} as the delivery location")
    public void theCampusUserSelectsAsTheDeliveryLocation(String deliveryLocationName) {
        deliveryLocationID = locationService.getLocationByName(deliveryLocationName).orElseThrow(
                () -> new IllegalArgumentException("Location not found")
        ).getId();
    }

    @And("the customer sets a delivery time {int} minutes from now")
    public void theCampusUserSetsADeliveryTimeMinutesFromNow(Integer duration) {
        deliveryTime = LocalDateTime.now().plusMinutes(duration);
    }

    @And("the campus user creates a group order")
    public void theCampusUserCreatesAGroupOrder() {
        try {
            String groupID = this.facade.createGroupOrder(this.userID, this.deliveryLocationID, this.deliveryTime);
            this.groupOrder = this.facade.getGroupOrderById(groupID);
        } catch (Exception exception) {
            this.exception = exception;
        }
    }

    @Then("a group order is created with the status {string}")
    public void aGroupOrderIsCreatedWithTheStatus(String expectedStatus) {
        assertTrue(expectedStatus.equalsIgnoreCase(this.groupOrder.getStatus().name()), "Group order status is not as expected");
    }

    @And("a unique group order identifier is generated")
    public void aUniqueGroupOrderIdentifierIsGenerated() {
        assertTrue(groupOrder.getGroupID() != null && !groupOrder.getGroupID().isEmpty(), "Group order ID is not generated");
    }

    @And("the group order has the delivery time set to {int} minutes from now")
    public void theGroupOrderHasTheDeliveryTimeSetToMinutesFromNow(Integer int1) {
        assertEquals(deliveryTime, groupOrder.getDeliveryTime());
    }

    @Then("the order opening error message {string} is displayed")
    public void theOrderOpeningErrorMessageIsDisplayed(String expectedErrorMessage) {
        assertEquals(expectedErrorMessage, this.exception.getMessage());
    }

    @And("the group order does not have a delivery time set")
    public void theGroupOrderDoesNotHaveADeliveryTimeSet() {
        assertNull(groupOrder.getDeliveryTime());
    }

}
