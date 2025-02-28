package fr.unice.polytech.application.port;

import fr.unice.polytech.domain.models.order.Order;

public interface IOrderRepository extends IReadRepository<Order>, IWriteRepository<Order> {

}
