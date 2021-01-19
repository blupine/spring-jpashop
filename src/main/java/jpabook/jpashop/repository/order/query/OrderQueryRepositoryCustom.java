package jpabook.jpashop.repository.order.query;

import java.util.List;

public interface OrderQueryRepositoryCustom {
    public List<OrderQueryDto> findOrderQueryDtos();
    public List<OrderQueryDto> findAllByDto_optimization();
    public List<OrderFlatDto> findAllByDto_flat();

}
