package fr.unice.polytech.application.usecase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import fr.unice.polytech.application.exceptions.EntityNotFoundException;
import fr.unice.polytech.application.port.IGroupOrderRepository;
import fr.unice.polytech.application.usecase.interfaces.IGroupOrderService;
import fr.unice.polytech.domain.models.groupOrder.GroupOrder;
import fr.unice.polytech.domain.models.groupOrder.GroupOrderStatus;

import static java.time.temporal.ChronoUnit.MINUTES;

public class GroupOrderService implements IGroupOrderService {

    private final IGroupOrderRepository groupOrderRepository;
    public static GroupOrderService instance;
    private static final String MINUTES_FROM_NOW = " minute(s) from now";

    public GroupOrderService(IGroupOrderRepository groupOrderRepository) {
        this.groupOrderRepository = groupOrderRepository;
    }

    @Override
    public List<GroupOrder> getAllGroupOrders() {
        return groupOrderRepository.findAll();
    }

    @Override
    public GroupOrder findGroupOrderById(String groupID) throws EntityNotFoundException {
        return groupOrderRepository.findById(groupID).orElseThrow(
                () -> {
                    Logger.getGlobal().severe("Failed to find GroupOrder with ID: " + groupID);
                    return new EntityNotFoundException("Group order", groupID);
                }
        );
    }

    @Override
    public GroupOrder findSubOrderGroup(String orderID) {
        for (GroupOrder groupOrder : getAllGroupOrders()) {
            for (String orderID2 : groupOrder.getSubOrderIDs()) {
                if (orderID2.equals(orderID)) {
                    return groupOrder;
                }
            }
        }
        return null;
    }

    @Override
    public String createGroupOrder(String orderID, String deliveryLocationID, LocalDateTime expectedDeliveryTime) throws IllegalArgumentException {
        if (deliveryLocationID == null) {
            Logger.getGlobal().warning("Invalid Location ID");
            throw new IllegalArgumentException("Invalid Location ID");
        } else if (expectedDeliveryTime != null && !expectedDeliveryTime.isAfter(LocalDateTime.now())) {
            Logger.getGlobal().warning("Invalid delivery time during group order creation");
            throw new IllegalArgumentException("Invalid delivery time!!");
        }

        GroupOrder groupOrder = new GroupOrder(deliveryLocationID, expectedDeliveryTime);
        groupOrder.setStatus(GroupOrderStatus.INITIALIZED);
        groupOrderRepository.add(groupOrder);

        groupOrder.addSubOrder(orderID);
        return groupOrder.getGroupID();
    }

    @Override
    public void joinGroupOrder(String orderID, String groupID) throws IllegalStateException, EntityNotFoundException {
        GroupOrder groupOrder = this.findGroupOrderById(groupID);
        if (groupOrder.getStatus() != GroupOrderStatus.COMPLETING && groupOrder.getStatus() != GroupOrderStatus.INITIALIZED) {
            Logger.getGlobal().warning("Group order already closed");
            throw new IllegalStateException("Group order already closed");
        }
        groupOrder.addSubOrder(orderID);
    }

    @Override
    public void completeOrderGroup(String orderID) {
        GroupOrder groupOrder = findSubOrderGroup(orderID);
        if (groupOrder != null) {
            groupOrder.setStatus(GroupOrderStatus.COMPLETING);
        }
    }

    @Override
    public void validateGroupOrder(String orderID, LocalDateTime expectedDeliveryTime, LocalDateTime closestPossibleDeliveryTime)
            throws IllegalArgumentException, IllegalStateException {
        GroupOrder groupOrder = findSubOrderGroup(orderID);

        if (groupOrder.getStatus() == GroupOrderStatus.FINALISING) {
            Logger.getGlobal().warning("Group order already validated");
            throw new IllegalStateException("Group order already validated");
        } else if (groupOrder.getStatus() != GroupOrderStatus.COMPLETING) {
            Logger.getGlobal().warning("Group order not in completing state");
            throw new IllegalStateException("Group order not in completing state");
        } else if (expectedDeliveryTime == null && groupOrder.getDeliveryTime() == null) {
            Logger.getGlobal().warning("Expected delivery time must be set");
            throw new IllegalArgumentException("Expected delivery time must be set");
        } else if (expectedDeliveryTime != null) {
            if (!expectedDeliveryTime.isAfter(LocalDateTime.now())) {
                Logger.getGlobal().warning("Invalid delivery time");
                throw new IllegalArgumentException("Invalid delivery time");
            }
            if (closestPossibleDeliveryTime.isAfter(expectedDeliveryTime)) {
                int additionalMinutes = ((int) (closestPossibleDeliveryTime.until(expectedDeliveryTime, MINUTES) / 10)) * 10 + 11;
                signalMinimumDeliveryDelay((int) LocalDateTime.now().until(closestPossibleDeliveryTime.plusMinutes(additionalMinutes), MINUTES));
            }
            groupOrder.setDeliveryTime(expectedDeliveryTime);
        }

        groupOrder.setStatus(GroupOrderStatus.FINALISING);
    }

    @Override
    public void confirmGroupOrder(String orderID) throws IllegalStateException {
        GroupOrder groupOrder = findSubOrderGroup(orderID);

        if (groupOrder.getStatus() == GroupOrderStatus.IN_PREPARATION) {
            Logger.getGlobal().warning("Group order already confirmed");
            throw new IllegalStateException("Group order already confirmed");
        } else if (groupOrder.getStatus() != GroupOrderStatus.FINALISING) {
            Logger.getGlobal().warning("Group order not in finalising state");
            throw new IllegalStateException("Group order not in finalising state");
        }

        groupOrder.setStatus(GroupOrderStatus.IN_PREPARATION);
        groupOrder.setConfirmationMoment(LocalDateTime.now());
    }

    @Override
    public void dropSubOrder(String orderID) throws EntityNotFoundException {
        if (orderID != null && findSubOrderGroup(orderID) != null) {
            findSubOrderGroup(orderID).getSubOrderIDs().remove(orderID);
        }
    }

    public void signalMinimumDeliveryDelay(int deliveryDelay) {
        int hourMinutes = 60;
        int dayMinutes = 24 * hourMinutes;

        String message = "Expected delivery time must be at least ";

        if (deliveryDelay < hourMinutes) {
            message += deliveryDelay + MINUTES_FROM_NOW;
        } else if (deliveryDelay < dayMinutes) {
            message += deliveryDelay / hourMinutes + " hour(s) and "
                    + deliveryDelay % hourMinutes + MINUTES_FROM_NOW;
        } else {
            message += deliveryDelay / dayMinutes + " day(s) and "
                    + (deliveryDelay % dayMinutes) / hourMinutes + " hour(s) and "
                    + deliveryDelay % hourMinutes + MINUTES_FROM_NOW;
        }
        Logger.getGlobal().warning(message);
        throw new IllegalArgumentException(message);
    }
}