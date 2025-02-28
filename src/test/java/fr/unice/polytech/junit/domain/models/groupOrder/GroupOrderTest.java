package fr.unice.polytech.junit.domain.models.groupOrder;

import fr.unice.polytech.domain.models.groupOrder.GroupOrder;
import fr.unice.polytech.domain.models.groupOrder.GroupOrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class GroupOrderTest {

    private String orderID;
    private String deliveryLocationID;
    private LocalDateTime expectedDeliveryTime;
    private GroupOrderStatus groupOrderStatus;

    private GroupOrder groupOrder;

    @BeforeEach
    void setUp() {
        this.orderID = "01234567";
        this.deliveryLocationID = "84635389";
        this.groupOrderStatus = GroupOrderStatus.INITIALIZED;
        this.expectedDeliveryTime = LocalDateTime.now().plusMinutes(60);
    }

    @Test
    void testCreateGroupOrder() {
        this.groupOrder = new GroupOrder(deliveryLocationID, null);
        assertNotNull(groupOrder.getGroupID());
        assertNotNull(groupOrder.getCreationMoment());
        assertEquals(0, groupOrder.getSubOrderIDs().size());
        assertEquals(deliveryLocationID, groupOrder.getDeliveryLocationID());
        assertNull(groupOrder.getDeliveryTime());
        assertEquals(groupOrderStatus, groupOrder.getStatus());
        assertEquals(0, groupOrder.getSubOrderIDs().size());

        this.groupOrder = new GroupOrder(deliveryLocationID, expectedDeliveryTime);
        assertNotNull(groupOrder.getGroupID());
        assertNotNull(groupOrder.getCreationMoment());
        assertEquals(0, groupOrder.getSubOrderIDs().size());
        assertEquals(deliveryLocationID, groupOrder.getDeliveryLocationID());
        assertEquals(expectedDeliveryTime, groupOrder.getDeliveryTime());
        assertEquals(groupOrderStatus, groupOrder.getStatus());
        assertEquals(0, groupOrder.getSubOrderIDs().size());
    }

    @Test
    void testAddSubOrder() {
        this.groupOrder = new GroupOrder(deliveryLocationID, null);
        assertEquals(0, groupOrder.getSubOrderIDs().size());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            groupOrder.addSubOrder(null);
        });
        assertEquals("Sub-order cannot be null", exception.getMessage());

        this.groupOrder.addSubOrder(orderID);
        assertEquals(1, groupOrder.getSubOrderIDs().size());
        assertTrue(groupOrder.getSubOrderIDs().contains(orderID));
    }

    @Test
    void testSetConfirmingMoment() {
        this.groupOrder = new GroupOrder(deliveryLocationID, null);

        LocalDateTime confirmingMoment = LocalDateTime.now();
        groupOrder.setConfirmationMoment(confirmingMoment);
        assertEquals(confirmingMoment, groupOrder.getConfirmationMoment());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            groupOrder.setConfirmationMoment(confirmingMoment);
        });
        assertEquals("Group order already confirmed.", exception.getMessage());
    }

    @Test
    void testSetExpectedDeliveryTime() {
        this.groupOrder = new GroupOrder(deliveryLocationID, null);

        LocalDateTime expectedDeliveryTime = LocalDateTime.now().plusMinutes(120);
        groupOrder.setDeliveryTime(expectedDeliveryTime);
        assertEquals(expectedDeliveryTime, groupOrder.getDeliveryTime());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            groupOrder.setDeliveryTime(expectedDeliveryTime);
        });
        assertEquals("Group order delivery time already set", exception.getMessage());
    }

}