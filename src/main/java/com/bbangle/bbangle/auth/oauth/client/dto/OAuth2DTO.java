package com.bbangle.bbangle.auth.oauth.client.dto;

import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OAuth2DTO {

    public static <T> OAuthParams getParams(HttpServletRequest request, Class<T> clazz) {
        String stateParam = request.getParameter("state");
        if (stateParam == null || stateParam.isBlank()) throw new OAuth2Exception(BbangleErrorCode.INVALID_OAUTH_PARAMS);

        String[] parts = stateParam.split("\\|");
        if (parts.length < 3) throw new OAuth2Exception(BbangleErrorCode.INVALID_OAUTH_PARAMS);

        String csrfState = safeBase64Decode(parts[0]);
        String user = safeBase64Decode(parts[1]);
        String profile = safeBase64Decode(parts[2]);

        OAuthParams dto = OAuthParams.builder()
            .user(user)
            .profile(profile)
            .build();

        log.info("[{}] - State:{} | params:{}", clazz.getSimpleName(), csrfState, dto);
        if (!dto.valid()) throw new OAuth2Exception(BbangleErrorCode.INVALID_OAUTH_PARAMS);

        return dto;
    }

    private static String safeBase64Decode(String encoded) {
        try {
            return new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new OAuth2Exception(BbangleErrorCode.INVALID_OAUTH_PARAMS, e);
        }
    }

    public enum UserType { CUSTOMER, SELLER }
    public enum ProfileType { LOCAL, PROD }

    @Builder
    public record OAuthParams(
        String user,    // customer | seller
        String profile  // local | prod
    ) {
        public boolean valid() {
            if (user == null || profile == null) return false;
            try {
                UserType.valueOf(user.toUpperCase());
                ProfileType.valueOf(profile.toUpperCase());
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        public UserType getUser() {
            return UserType.valueOf(user.toUpperCase());
        }

        public ProfileType getProfile() {
            return ProfileType.valueOf(profile.toUpperCase());
        }
    }

    @Builder
    public record InfoDTO(
        Long id,
        Role role,
        CertificationStatus status
    ) {}
}
