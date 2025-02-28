package fr.unice.polytech.infrastructure.repository.inmemory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.unice.polytech.application.port.IOrderRepository;
import fr.unice.polytech.domain.models.order.Order;

import fr.unice.polytech.infrastructure.repository.exceptions.EntityAlreadyExistsException;

public class OrderRepository implements IOrderRepository {
    private final Map<String, Order> orders;

    public OrderRepository() {
        this.orders = new HashMap<>();
    }

    @Override
    public Order add(Order order) {
        if (orders.containsKey(order.getId())) {
            throw new EntityAlreadyExistsException("Order with id " + order.getId() + " already exists");
        }
        orders.put(order.getId(), order);
        return order;
    }

    @Override
    public void update(Order order) {
        orders.put(order.getId(), order);
    }

    @Override
    public void remove(Order order) {
        orders.remove(order.getId());
    }

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(orders.get(id));
    }

    @Override
    public List<Order> findAll() {
        return orders.values().stream().toList();
    }


}