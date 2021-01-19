package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    MemberRepository memberRepositoryOld;
    @Autowired MemberService memberService;

    @Test
    public void 회원가입() throws Exception {
        // given
        Member member = new Member();
        member.setName("testName");

        // when
        Long id = memberService.join(member);

        // then
        assertEquals(member, memberRepositoryOld.findOne(id));
    }

    @Test
    public void 중복_회원가입() throws Exception {
        // given
        Member member = new Member();
        member.setName("testName");

        Member member2 = new Member();
        member2.setName("testName");

        // when
        memberService.join(member);
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> memberService.join(member2));

        // then
        assertEquals(thrown.getMessage(), "이미 존재하는 회원입니다.");
    }
}