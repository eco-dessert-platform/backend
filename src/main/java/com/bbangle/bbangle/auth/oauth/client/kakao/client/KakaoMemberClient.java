package com.bbangle.bbangle.auth.oauth.client.kakao.client;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.auth.oauth.client.OAuthMemberClient;
import com.bbangle.bbangle.auth.oauth.client.kakao.dto.KakaoMemberResponse;
import com.bbangle.bbangle.member.domain.Member;
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
