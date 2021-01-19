package jpabook.jpashop.repository.order.query;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static jpabook.jpashop.domain.QDelivery.delivery;
import static jpabook.jpashop.domain.QMember.member;
import static jpabook.jpashop.domain.QOrder.*;
import static jpabook.jpashop.domain.QOrderItem.orderItem;
import static jpabook.jpashop.domain.item.QItem.item;

@Repository
public class OrderQueryRepository {

    private final EntityManager em;
    private final JPAQueryFactory qFactory;

    public OrderQueryRepository(EntityManager em) {
        this.em = em;
        qFactory = new JPAQueryFactory(em);
    }

    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders(); // query : 1, result : N

        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId()); // query : N
            o.setOrderItems(orderItems);
        });
        return result;
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return qFactory.select(Projections.constructor(OrderItemQueryDto.class,
                orderItem.order.id, item.name, orderItem.orderPrice, orderItem.count))
                .from(orderItem)
                .leftJoin(orderItem.item, item)
                .where(orderItem.order.id.eq(orderId))
                .fetch();
    }

    private List<OrderQueryDto> findOrders() {
        return qFactory
                .select(Projections.constructor(OrderQueryDto.class,
                        order.id, member.name, order.orderDate, order.status, delivery.address))
                .from(order)
                .leftJoin(order.member, member)
                .leftJoin(order.delivery, delivery)
                .fetch();
    }


    /**
     * N+1 문제를 해결한 V5 버전 (총 쿼리 2회)
     * @return
     */
    public List<OrderQueryDto> findAllByDto_optimization() {
        List<OrderQueryDto> orderDtos = findOrders();

        List<Long> orderIds = toOrderIds(orderDtos);

        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(orderIds);

        orderDtos.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return orderDtos;
    }

    private List<Long> toOrderIds(List<OrderQueryDto> orderDtos) {
        List<Long> orderIds = orderDtos.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
        return orderIds;
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = qFactory.select(Projections.constructor(OrderItemQueryDto.class,
                orderItem.order.id, item.name, orderItem.orderPrice, orderItem.count))
                .from(orderItem)
                .leftJoin(orderItem.item, item)
                .where(orderItem.order.id.in(orderIds))
                .fetch();

        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItemQueryDto -> OrderItemQueryDto.getOrderId()));
        return orderItemMap;
    }

    /**
     * 쿼리 1회로 가져오는 V6 버전
     * @return
     */
    public List<OrderFlatDto> findAllByDto_flat() {
        return qFactory.select(Projections.constructor(OrderFlatDto.class,
                order.id, member.name, order.orderDate, order.status, delivery.address, item.name, orderItem.orderPrice, orderItem.count))
                .from(order)
                .leftJoin(order.member, member)
                .leftJoin(order.delivery, delivery)
                .leftJoin(order.orderItems, orderItem)
                .leftJoin(orderItem.item, item)
                .fetch();
    }
}
