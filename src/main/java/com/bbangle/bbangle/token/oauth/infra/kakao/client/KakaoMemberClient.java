package com.bbangle.bbangle.token.oauth.infra.kakao.client;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.token.oauth.domain.OauthServerType;
import com.bbangle.bbangle.token.oauth.domain.client.OAuthMemberClient;
import com.bbangle.bbangle.token.oauth.infra.kakao.dto.KakaoMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoMemberClient implements OAuthMemberClient {

    private final KakaoApiClient kakaoApiClient;

    @Override
    public Member fetch(String token) {
        KakaoMemberResponse kakaoMemberResponse = kakaoApiClient.fetchMember("Bearer " + token);
        return kakaoMemberResponse.toMember();
    }

    @Override
    public OauthServerType supportServer() {
        return OauthServerType.KAKAO;
    }

}
