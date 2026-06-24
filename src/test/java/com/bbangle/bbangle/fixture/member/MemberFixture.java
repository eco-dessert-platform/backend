package com.bbangle.bbangle.fixture.member;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.member.domain.Member;

public class MemberFixture {

    public static final String DEFAULT_MEMBER_EMAIL = "123@test.com";
    public static final String DEFAULT_MEMBER_PHONE = "01012345678";
    public static final String DEFAULT_MEMBER_NAME = "test";
    public static final String DEFAULT_MEMBER_NICKNAME = "temp";
    public static final String DEFAULT_MEMBER_BIRTH = "2026-01-01";
    public static final String DEFAULT_MEMBER_PROFILE = "test.png";

    private MemberFixture() {}

    private static Member baseBuilder(String email, String phone, String name, String nickname, String birth, String profile) {
        return Member.builder()
            .providerId("12345")
            .provider(OauthServerType.GOOGLE)
            .email(email)
            .phone(phone)
            .name(name)
            .nickname(nickname)
            .birth(birth)
            .profile(profile)
            .build();
    }

    public static Member defaultMember() {
        return baseBuilder(
            DEFAULT_MEMBER_EMAIL, DEFAULT_MEMBER_PHONE, DEFAULT_MEMBER_NAME, DEFAULT_MEMBER_NICKNAME, DEFAULT_MEMBER_BIRTH, DEFAULT_MEMBER_PROFILE
        );
    }

    public static Member createMemberWithName(String name) {
        return baseBuilder(
            DEFAULT_MEMBER_EMAIL, DEFAULT_MEMBER_PHONE, name, DEFAULT_MEMBER_NICKNAME, DEFAULT_MEMBER_BIRTH, DEFAULT_MEMBER_PROFILE
        );
    }
}
