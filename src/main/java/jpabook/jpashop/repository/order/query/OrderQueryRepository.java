package jpabook.jpashop.repository.order.query;

import jpabook.jpashop.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderQueryRepository extends JpaRepository<Order, Long>, OrderQueryRepositoryCustom {
}
