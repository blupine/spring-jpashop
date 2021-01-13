package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired EntityManager em;
    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception {
        // given
        Member member = createMember("name1", "seroul", "gyeongi", "101001");
        em.persist(member);

        Book book = createBook("book1", 10000, 10);
        em.persist(book);

        // when
        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // then
        Order order = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.ORDER, order.getStatus());
        assertEquals(1, order.getOrderItems().size());
        assertEquals(10000 * orderCount, order.getTotalPrice());
        assertEquals(10 - orderCount, book.getStockQuantity());
    }

    @Test
    public void 주문취소() throws Exception {
        // given
        Member member = createMember("name1", "seroul", "gyeongi", "101001");
        em.persist(member);

        Book book = createBook("book1", 10000, 10);
        em.persist(book);

        int orderCount = 3;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // when
        orderService.cancelOrder(orderId);

        // then
        Order order = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.CANCEL, order.getStatus());
        assertEquals(10, book.getStockQuantity());

    }

    @Test
    public void 상품주문_재고수량초과() throws Exception {
        // given
        Member member = createMember("name1", "seroul", "gyeongi", "101001");
        em.persist(member);

        Book book = createBook("book1", 10000, 10);
        em.persist(book);

        // when
        NotEnoughStockException thrown = assertThrows(NotEnoughStockException.class,
                () -> orderService.order(member.getId(), book.getId(), book.getStockQuantity() + 1));

        // then
        assertEquals(thrown.getMessage(), "need more stock");
    }

    public Book createBook(String name, int price, int stockQuantity){
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        return book;
    }

    public Member createMember(String name, String city, String street, String zipcode) {
        Member m = new Member();
        m.setName(name);
        m.setAddress(new Address(city, street, zipcode));
        return m;
    }
}