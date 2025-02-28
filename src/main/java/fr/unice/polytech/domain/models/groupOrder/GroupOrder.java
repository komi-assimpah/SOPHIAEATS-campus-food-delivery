package fr.unice.polytech.domain.models.groupOrder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class GroupOrder {

    private final String groupID;
    private final LocalDateTime creationMoment;
    private LocalDateTime confirmationMoment;
    private final String deliveryLocationID;
    @JsonProperty("deliveryTime")
    private LocalDateTime deliveryTime;
    private GroupOrderStatus status;
    private final List<String> orderIDs;

    @JsonCreator
    public GroupOrder(

            @JsonProperty("deliveryLocationID") String deliveryLocationID,
            @JsonProperty("deliveryTime") LocalDateTime deliveryTime
    ) {
        this.groupID = UUID.randomUUID().toString();
        this.deliveryLocationID = deliveryLocationID;
        this.deliveryTime = deliveryTime;
        this.creationMoment = LocalDateTime.now();
        this.status = GroupOrderStatus.INITIALIZED;
        this.orderIDs = new ArrayList<>();

    }

    public GroupOrder() {
        this.creationMoment =null;
        this.orderIDs = null;
        this.groupID = null;
        this.deliveryLocationID = null;
        this.status = null;

    }

    public String getGroupID() {
        return groupID;
    }

    public LocalDateTime getCreationMoment() {
        return creationMoment;
    }

    public LocalDateTime getConfirmationMoment() {
        return confirmationMoment;
    }

    public String getDeliveryLocationID() {
        return deliveryLocationID;
    }

    public LocalDateTime getDeliveryTime() {
        return deliveryTime;
    }

    public GroupOrderStatus getStatus() {
        return status;
    }

    public List<String> getSubOrderIDs() {
        return orderIDs;
    }

    public void setConfirmationMoment(LocalDateTime confirmationMoment) {
        if (this.confirmationMoment != null) {
            Logger.getGlobal().warning("Group order already confirmed.");
            throw new IllegalStateException("Group order already confirmed.");
        }
        this.confirmationMoment = confirmationMoment;
    }

    public void setDeliveryTime(LocalDateTime deliveryTime) {
        if (this.deliveryTime != null) {
            Logger.getGlobal().warning("Group order delivery time already set");
            throw new IllegalStateException("Group order delivery time already set");
        }
        this.deliveryTime = deliveryTime;
    }

    public void setStatus(GroupOrderStatus status) {
        this.status = status;
    }

    public void addSubOrder(String orderID) {
        if (orderID == null) {
            Logger.getGlobal().warning("Sub-order cannot be null");
            throw new IllegalArgumentException("Sub-order cannot be null");
        }
        this.orderIDs.add(orderID);
    }

}
