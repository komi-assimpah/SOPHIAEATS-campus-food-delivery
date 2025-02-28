package fr.unice.polytech.domain.models.restaurant.discountstrategy;

import fr.unice.polytech.domain.models.order.Order;

public abstract class DiscountStrategy {
    public abstract void applyDiscount(Order order);

    /**
     * Override the equals method to compare two DiscountStrategy objects <br>
     * Two DiscountStrategy objects are considered equal if their attributes are equal (ex: percentage, quantity, ...) <br>
     * The is needed to enable the use of DiscountStrategy objects in collections <br>
     *
     */
    public abstract boolean equals(Object other);
    public abstract int hashCode();
}
