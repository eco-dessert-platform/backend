package com.bbangle.bbangle.fixture.member.domain;

import com.bbangle.bbangle.member.domain.Member;
import org.springframework.test.util.ReflectionTestUtils;

public final class MemberFixture {

    private MemberFixture() {
    }

    /** 저장 가능한 구매자. 식별자는 영속화 시점에 채워진다. */
    public static Member defaultBuyer() {
        return Member.builder()
            .email("buyer@example.com")
            .phone("01012345678")
            .name("홍길동")
            .nickname("길동")
            .build();
    }

    public static Member createWithId(Long id) {
        Member member = Member.builder().build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}
