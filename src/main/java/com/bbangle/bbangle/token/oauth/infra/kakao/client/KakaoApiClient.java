package com.bbangle.bbangle.token.oauth.infra.kakao.client;

import com.bbangle.bbangle.token.oauth.infra.kakao.dto.KakaoMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
public class KakaoApiClient {

    private static final String USERINFO_URL = "https://kapi.kakao.com/v2/user/me";
    private final RestTemplate restTemplate = new RestTemplate();

    public KakaoMemberResponse fetchMember(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        HttpEntity<String> request = new HttpEntity<>("", headers);
        ResponseEntity<KakaoMemberResponse> response = restTemplate.exchange(USERINFO_URL,
            HttpMethod.GET, request, KakaoMemberResponse.class);
        return response.getBody();
    }

}
