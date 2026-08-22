package com.bbangle.bbangle.fixture.member.domain;

import com.bbangle.bbangle.member.domain.Member;
import org.springframework.test.util.ReflectionTestUtils;

public final class MemberFixture {

    private MemberFixture() {
    }

    public static Member createWithId(Long id) {
        Member member = Member.builder().build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}
