package com.bbangle.bbangle.testutil.model;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.token.oauth.domain.OauthServerType;

public class TestMember extends TestModel<Member>{
    private Long id = null;
    private String email = "test@test.com";
    private String phone = "01000000000";
    private String name = "testName";
    private String nickname = "testNickname";
    private String birth = "201231";
    private String profile = "/profile.jpg";
    private OauthServerType provider = OauthServerType.KAKAO;

    public TestMember setId(Long id) {
        this.id = id;

        return this;
    }

    public TestMember setEmail(String email) {
        this.email = email;

        return this;
    }

    public TestMember setPhone(String phone) {
        this.phone = phone;

        return this;
    }

    public TestMember setName(String name) {
        this.name = name;

        return this;
    }

    public TestMember setNickname(String nickname) {
        this.nickname = nickname;

        return this;
    }

    public TestMember setBirth(String birth) {
        this.birth = birth;

        return this;
    }

    public TestMember setProfile(String profile) {
        this.profile = profile;

        return this;
    }

    public TestMember setProvider(OauthServerType provider) {
        this.provider = provider;

        return this;
    }

    @Override
    public Member getModel() {
        return Member.builder()
                .email(email)
                .phone(phone)
                .name(name)
                .nickname(nickname)
                .birth(birth)
                .profile(profile)
                .provider(provider)
                .build();
    }
}
