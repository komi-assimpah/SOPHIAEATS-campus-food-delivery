package fr.unice.polytech.domain.models.restaurant.discountstrategy;

import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.user.UserStatus;

import java.util.Objects;

public class StatusDiscount extends DiscountStrategy {

    private final double discountPercentage;
    private final UserStatus userStatusToWhichDiscountApplies;

    public StatusDiscount(UserStatus userStatusToWhichDiscountApplies, double discountPercentage) {
        if (discountPercentage < 0 || discountPercentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }
        this.discountPercentage = discountPercentage / 100;
        this.userStatusToWhichDiscountApplies = userStatusToWhichDiscountApplies;
    }

    @Override
    public void applyDiscount(Order order) {
        UserStatus userOrderingStatus = order.getUser().getType();
        if (userOrderingStatus == userStatusToWhichDiscountApplies) {
            double discountAmount = order.getTotalAmount() * discountPercentage;
            order.getUser().addToBalance(discountAmount);
            System.out.println(order.getUser().getBalance());
            System.out.println("Since you are a " + userOrderingStatus + ", the restaurant has offered you a discount of " + (discountPercentage * 100) + "%.");
            System.out.println("A discount of " + discountAmount + " has been applied. Thanks to the restaurant manager.");
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        StatusDiscount that = (StatusDiscount) other;
        return discountPercentage == that.discountPercentage && userStatusToWhichDiscountApplies == that.userStatusToWhichDiscountApplies;
    }

    @Override
    public int hashCode() {
        return Objects.hash(discountPercentage, userStatusToWhichDiscountApplies);
    }
}
