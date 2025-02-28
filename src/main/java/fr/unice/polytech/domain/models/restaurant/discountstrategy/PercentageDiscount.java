package fr.unice.polytech.domain.models.restaurant.discountstrategy;

import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.order.OrderItem;

import java.util.Objects;
import java.util.logging.Logger;

public class PercentageDiscount extends DiscountStrategy {

    private final int percentage;
    private final int quantity;

    public PercentageDiscount(int percentage, int quantity) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }
        this.percentage = percentage;
        this.quantity = quantity;
    }

    @Override
    public void applyDiscount(Order order) {
        int totalItemQuantity = order.getOrderItems().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        if (!order.isSubOrder() && totalItemQuantity >= quantity) {
            Logger.getGlobal().info("Percentage discount applied to order with ID: " + order.getId());
            Logger.getGlobal().info("Initial total price: " + order.getTotalAmount());

            double discountAmount = order.getTotalAmount() * (percentage / 100.0);

            /*double newTotalAmount = order.getTotalAmount() - discountAmount;
            // FIXME:The total amount should not be updated - the discount should be applied to the user's balance only
            //order.setTotalAmount(newTotalAmount);*/

            order.getUser().addToBalance(discountAmount);

            Logger.getGlobal().info("\nA discount of " + discountAmount + " has been applied.\nNew User balance: "
                    + order.getUser().getBalance() + "\n" + "Thanks to the restaurant manager!\n");
        }
    }

    public void applyGroupDiscount(Order order, int totalItemQuantity) {
        Logger.getGlobal().info("Group percentage discount applied to order with ID: " + order.getId());
        Logger.getGlobal().info("Initial total price: " + order.getTotalAmount());

        double discountAmount = order.getTotalAmount() * (percentage / 100.0);
        if (totalItemQuantity >= quantity) {
            order.getUser().addToBalance(discountAmount);
        }

        Logger.getGlobal().info("\nA discount of " + discountAmount + " has been applied.\nNew User balance: "
                + order.getUser().getBalance() + "\n" + "Thanks to the restaurant manager!\n");
    }

    public int getPercentage() {
        return percentage;
    }

    public int getQuantity() {
        return quantity;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PercentageDiscount that = (PercentageDiscount) o;
        return percentage == that.percentage && quantity == that.quantity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(percentage, quantity);
    }
}
