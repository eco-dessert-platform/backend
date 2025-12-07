package com.bbangle.bbangle.auth.oauth.client.google.client;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.auth.oauth.client.OAuthMemberClient;
import com.bbangle.bbangle.auth.oauth.client.google.dto.GoogleMemberResponse;
import com.bbangle.bbangle.member.domain.Member;
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
