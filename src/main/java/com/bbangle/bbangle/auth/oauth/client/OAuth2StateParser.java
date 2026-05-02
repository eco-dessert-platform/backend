package com.bbangle.bbangle.auth.oauth.client;

import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO.OAuthParams;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO.ProfileType;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO.UserType;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OAuth2StateParser {

    // state 파라미터 값을 추출한 후 . 문자를 기준으로 파싱
    public OAuthParams getParams(String stateParam) {
        if (stateParam == null || stateParam.isBlank()) throw new OAuth2Exception(BbangleErrorCode.INVALID_OAUTH_PARAMS);

        String[] parts = stateParam.split("\\.");
        if (parts.length < 3) throw new OAuth2Exception(BbangleErrorCode.INVALID_OAUTH_PARAMS);

        // parts[1] : user 파라미터 값
        // parts[2] : profile 파라미터 값
        return getParams(parts[1], parts[2]);
    }

    // OAuth2 파라미터 검증 및 OAuthParams DTO 생성 메서드
    public OAuthParams getParams(String user, String profile) {
        try {
            return OAuthParams.builder()
                .user(UserType.valueOf(user.toUpperCase()))
                .profile(ProfileType.valueOf(profile.toUpperCase()))
                .build();
        } catch (Exception e) {
            throw new OAuth2Exception(BbangleErrorCode.INVALID_OAUTH_PARAMS, e);
        }
    }
}
