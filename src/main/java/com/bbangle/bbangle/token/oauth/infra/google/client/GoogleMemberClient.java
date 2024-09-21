package com.bbangle.bbangle.token.oauth.infra.google.client;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.token.oauth.domain.OauthServerType;
import com.bbangle.bbangle.token.oauth.domain.client.OAuthMemberClient;
import com.bbangle.bbangle.token.oauth.infra.google.dto.GoogleMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GoogleMemberClient implements OAuthMemberClient {

    private final GoogleApiClient googleApiClient;

    @Override
    public Member fetch(String token) {
        GoogleMemberResponse googleMemberResponse = googleApiClient.fetchMember("Bearer " + token);
        return googleMemberResponse.toMember();
    }

    @Override
    public OauthServerType supportServer() {
        return OauthServerType.GOOGLE;
    }
}
