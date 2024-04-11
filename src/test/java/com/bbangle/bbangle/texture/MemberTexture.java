package com.bbangle.bbangle.texture;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.token.oauth.domain.OauthServerType;
import net.datafaker.Faker;

public class MemberTexture {

    private static final Faker faker = new Faker();

    public static Member createKakaoMember(){
        String randomProviderId = String.valueOf(faker.random()
            .nextLong(0, 10_000_000));

        return Member.builder()
            .provider(OauthServerType.KAKAO)
            .providerId(randomProviderId)
            .build();
    }

}
