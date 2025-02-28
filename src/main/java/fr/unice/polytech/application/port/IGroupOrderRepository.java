package fr.unice.polytech.application.port;

import fr.unice.polytech.domain.models.groupOrder.GroupOrder;

public interface IGroupOrderRepository extends IReadRepository<GroupOrder>, IWriteRepository<GroupOrder> {
}
