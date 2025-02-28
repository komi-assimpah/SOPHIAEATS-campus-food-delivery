package fr.unice.polytech.application.dto;

import fr.unice.polytech.domain.models.groupOrder.GroupOrder;
import fr.unice.polytech.domain.models.groupOrder.GroupOrderStatus;

import java.time.format.DateTimeFormatter;
import java.util.*;

public class GroupOrderDTO {
    private String groupID;
    private String creationMoment;
    private String confirmationMoment;
    private String deliveryLocationID;
    private String deliveryTime;
    private GroupOrderStatus status;
    private List<String> orderIDs;

    public GroupOrderDTO(GroupOrder groupOrder) {
        this.groupID = groupOrder.getGroupID() != null ? groupOrder.getGroupID() : "";
        this.creationMoment = groupOrder.getCreationMoment() != null
                ? groupOrder.getCreationMoment().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : "0000-01-01T00:00:00";
        this.confirmationMoment = groupOrder.getConfirmationMoment() != null
                ? groupOrder.getConfirmationMoment().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : "0000-01-01T00:00:00";
        this.deliveryLocationID = groupOrder.getDeliveryLocationID() != null ? groupOrder.getDeliveryLocationID() : "";
        this.deliveryTime = groupOrder.getDeliveryTime() != null
                ? groupOrder.getDeliveryTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : "0000-01-01T00:00:00";
        this.status = groupOrder.getStatus() != null ? groupOrder.getStatus() : GroupOrderStatus.INITIALIZED;
        this.orderIDs = groupOrder.getSubOrderIDs() != null ? groupOrder.getSubOrderIDs() : Collections.emptyList();
    }

    public GroupOrderDTO() {
    }

    public String getGroupID() {
        return groupID;
    }

    public String getCreationMoment() {
        return creationMoment;
    }

    public String getConfirmationMoment() {
        return confirmationMoment;
    }

    public String getDeliveryLocationID() {
        return deliveryLocationID;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public GroupOrderStatus getStatus() {
        return status;
    }

    public List<String> getOrderIDs() {
        return orderIDs;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public void setCreationMoment(String creationMoment) {
        this.creationMoment = creationMoment;
    }

    public void setConfirmationMoment(String confirmationMoment) {
        this.confirmationMoment = confirmationMoment;
    }

    public void setDeliveryLocationID(String deliveryLocationID) {
        this.deliveryLocationID = deliveryLocationID;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public void setStatus(GroupOrderStatus status) {
        this.status = status;
    }

    public void setOrderIDs(List<String> orderIDs) {
        this.orderIDs = orderIDs;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("groupID", this.groupID);
        result.put("creationMoment", this.creationMoment);
        result.put("confirmationMoment", this.confirmationMoment);
        result.put("deliveryLocationID", this.deliveryLocationID);
        result.put("deliveryTime", this.deliveryTime);
        result.put("status", this.status != null ? this.status.toString() : null); // Assurez-vous que l'énumération est convertie en chaîne
        result.put("orderIDs", this.orderIDs != null ? new ArrayList<>(this.orderIDs) : Collections.emptyList()); // Éviter la mutation de la liste
        return result;
    }

}
