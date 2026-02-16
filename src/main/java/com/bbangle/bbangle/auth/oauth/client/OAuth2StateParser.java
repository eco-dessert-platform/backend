package com.bbangle.bbangle.auth.oauth.client;

import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO.OAuthParams;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO.ProfileType;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO.UserType;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OAuth2StateParser {

    private String safeBase64Decode(String encoded) {
        try {
            return new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new OAuth2Exception(BbangleErrorCode.INVALID_OAUTH_PARAMS, e);
        }
    }

    public <T> OAuthParams getParams(String stateParam, Class<T> clazz) {
        if (stateParam == null || stateParam.isBlank()) throw new OAuth2Exception(BbangleErrorCode.INVALID_OAUTH_PARAMS);

        String[] parts = stateParam.split("\\|");
        if (parts.length < 3) throw new OAuth2Exception(BbangleErrorCode.INVALID_OAUTH_PARAMS);

        String csrfState = safeBase64Decode(parts[0]);
        String user = safeBase64Decode(parts[1]);
        String profile = safeBase64Decode(parts[2]);

        OAuthParams dto = getParams(user, profile);
        log.info("[{}] - State:{} | params:{}", clazz.getSimpleName(), csrfState, dto);
        return dto;
    }

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
