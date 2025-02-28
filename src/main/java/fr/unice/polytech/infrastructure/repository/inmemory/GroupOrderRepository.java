package fr.unice.polytech.infrastructure.repository.inmemory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.unice.polytech.application.port.IGroupOrderRepository;
import fr.unice.polytech.domain.models.groupOrder.GroupOrder;

public class GroupOrderRepository implements IGroupOrderRepository {

    public static GroupOrderRepository instance;
    private final Map<String, GroupOrder> groupOrders;

    public GroupOrderRepository() {
        groupOrders = new HashMap<>();
    }

    public static synchronized GroupOrderRepository getInstance() {
        if (instance == null) {
            instance = new GroupOrderRepository();
        }
        return instance;
    }

    @Override
    public Optional<GroupOrder> findById(String id) {
        return Optional.ofNullable(groupOrders.get(id));
    }

    @Override
    public List<GroupOrder> findAll() {
        return groupOrders.values().stream().toList();
    }

    @Override
    public GroupOrder add(GroupOrder entity) {
        return groupOrders.put(entity.getGroupID(), entity);
    }

    @Override
    public void update(GroupOrder entity) {
        groupOrders.put(entity.getGroupID(), entity);
    }

    @Override
    public void remove(GroupOrder entity) {
        groupOrders.remove(entity.getGroupID());
    }

}