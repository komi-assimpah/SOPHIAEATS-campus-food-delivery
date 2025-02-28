package fr.unice.polytech.domain.models.restaurant.discountstrategy;

import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.order.OrderItem;
import fr.unice.polytech.domain.models.restaurant.MenuItem;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class Buy1Get1Strategy extends DiscountStrategy {
    private Logger logger = Logger.getLogger(Buy1Get1Strategy.class.getName());
    private final MenuItem itemToWhichDiscountApplies;

    public Buy1Get1Strategy(MenuItem itemToWhichDiscountApplies) {
        this.itemToWhichDiscountApplies = itemToWhichDiscountApplies;
    }

    @Override
    public void applyDiscount(Order order) {

        List<OrderItem> items = order.getOrderItems();
        double totalPrice = order.getTotalAmount();
        logger.info(() -> "order total price: " + totalPrice);


        OrderItem applicableItem = items.stream()
                .filter(orderItem -> orderItem.getItem().equals(itemToWhichDiscountApplies))
                .findFirst()
                .orElse(null);

        if (applicableItem != null) {
            int quantity = applicableItem.getQuantity();


            if (quantity > 1) {
                int discountQuantity = quantity ;

                logger.info(() -> "Initial price without discount: " + totalPrice);
                logger.info(() -> "You are paying: " + (totalPrice));
                logger.info(() -> "You have benefited from a Buy 1 Get 1 discount. You received " + discountQuantity + " free items of " + itemToWhichDiscountApplies.getName() + "!");
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Buy1Get1Strategy that = (Buy1Get1Strategy) other;
        return itemToWhichDiscountApplies.equals(that.itemToWhichDiscountApplies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemToWhichDiscountApplies);
    }

}
