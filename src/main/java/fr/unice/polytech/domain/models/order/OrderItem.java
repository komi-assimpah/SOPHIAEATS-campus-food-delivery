package fr.unice.polytech.domain.models.order;

import java.util.Objects;

import fr.unice.polytech.domain.models.restaurant.MenuItem;
import org.mockito.internal.matchers.Or;

public class OrderItem {
    private  MenuItem item;
    private int quantity;

    public OrderItem(MenuItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    OrderItem(){

    }

    public MenuItem getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return item.getPrice() * quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(item, orderItem.item);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(item);
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "item=" + item +
                ", quantity=" + quantity +
                '}';
    }
}
