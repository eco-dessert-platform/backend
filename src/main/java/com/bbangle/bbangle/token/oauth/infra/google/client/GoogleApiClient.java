package com.bbangle.bbangle.token.oauth.infra.google.client;

import com.bbangle.bbangle.token.oauth.infra.google.dto.GoogleMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
public class GoogleApiClient {
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v1/userinfo" ;
    private final RestTemplate restTemplate = new RestTemplate();

    public GoogleMemberResponse fetchMember(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        HttpEntity<String> request = new HttpEntity<>("", headers);
        ResponseEntity<GoogleMemberResponse> response = restTemplate.exchange(USERINFO_URL,
                HttpMethod.GET, request, GoogleMemberResponse.class);
        return response.getBody();
    }
}
