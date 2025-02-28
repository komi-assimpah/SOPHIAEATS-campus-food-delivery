package fr.unice.polytech.application.usecase.interfaces;


import fr.unice.polytech.application.exceptions.EntityNotFoundException;
import fr.unice.polytech.domain.models.delivery.DeliveryLocation;
import fr.unice.polytech.domain.models.groupOrder.GroupOrder;
import fr.unice.polytech.domain.models.order.Order;

import java.time.LocalDateTime;
import java.util.List;


/**
 * GroupOrderService will handle the process of creating, joining and closing group orders.
 */
public interface IGroupOrderService {

    List<GroupOrder> getAllGroupOrders();

    GroupOrder findSubOrderGroup(String orderID);

    GroupOrder findGroupOrderById(String groupID);


    String createGroupOrder(String orderID, String deliveryLocationID, LocalDateTime expectedDeliveryTime);

    void joinGroupOrder(String orderID, String groupID);

    void completeOrderGroup(String orderID);

    void validateGroupOrder(String orderID, LocalDateTime deliveryTime, LocalDateTime closestPossibleDeliveryTime);

    void confirmGroupOrder(String orderID);

    void dropSubOrder(String orderID);

}

