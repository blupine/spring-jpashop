package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;

import java.util.List;

public interface OrderRepositoryCustom {
    public List<Order> findAllBySearch(OrderSearch orderSearch);
    public List<Order> findAllWithItem();
    public List<Order> findAllWithMemberDelivery();
    public List<Order> findAllWithMemberDelivery(int offset, int limit);
}
