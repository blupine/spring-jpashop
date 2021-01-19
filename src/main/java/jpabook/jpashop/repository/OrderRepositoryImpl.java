package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;

import static jpabook.jpashop.domain.QDelivery.delivery;
import static jpabook.jpashop.domain.QMember.member;
import static jpabook.jpashop.domain.QOrder.order;
import static jpabook.jpashop.domain.QOrderItem.orderItem;
import static jpabook.jpashop.domain.item.QItem.item;

public class OrderRepositoryImpl implements OrderRepositoryCustom{

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public OrderRepositoryImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    /**
     * QueryDsl 사용하여 동적쿼리 처리
     * @param orderSearch
     * @return
     */
    @Override
    public List<Order> findAllBySearch(OrderSearch orderSearch) {
        return queryFactory.select(order)
                .from(order)
                .join(order.member, member)
                .where(statusEq(orderSearch.getOrderStatus()), nameLike(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();
    }

    /**
     * distinct로 중복을 제거하는건 애플리케이션으로 가져와서 제거하는 것
     * 따라서 중복이 많을 경우엔 DB에서 많은 데이터를 받아와야 한다는 한계가 있음 -> 이런 경우 batch fetch를 사용하는 편이 더 좋음
     * 또한 fetch join을 사용하기 때문에 paging 사용이 불가능함
     * @return
     */
    @Override
    public List<Order> findAllWithItem() {
        return queryFactory.selectDistinct(order)
                .from(order)
                .leftJoin(order.member, member).fetchJoin()
                .leftJoin(order.delivery, delivery).fetchJoin()
                .leftJoin(order.orderItems, orderItem).fetchJoin()
                .leftJoin(orderItem.item, item).fetchJoin()
                .fetch();
    }

    @Override
    public List<Order> findAllWithMemberDelivery() {
        return queryFactory.select(order)
                .from(order)
                .leftJoin(order.member, member).fetchJoin()
                .leftJoin(order.delivery, delivery).fetchJoin()
                .fetch();
    }

    /**
     * fetch join의 paging 문제와 데이터 전송량 문제를 해결할 수 있는 방법
     * application.yml의 hibernate.properties.default_batch_fetch_size 설정 필요
     * @param offset
     * @param limit
     * @return
     */
    @Override
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return queryFactory.select(order)
                .from(order)
                .leftJoin(order.member, member).fetchJoin()
                .leftJoin(order.delivery, delivery).fetchJoin()
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    private BooleanExpression statusEq(OrderStatus statusCond) {
        if(statusCond == null){
            return null;
        }
        return order.status.eq(statusCond);
    }

    private BooleanExpression nameLike(String memberName) {
        if (!StringUtils.hasText(memberName)) {
            return null;
        }
        return member.name.like(memberName);
    }

}
