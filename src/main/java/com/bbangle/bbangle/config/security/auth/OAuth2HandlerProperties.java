package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO.OAuthParams;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO.ProfileType;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO.UserType;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import java.util.UUID;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("oauth2")
public record OAuth2HandlerProperties (
    RedirectUrl redirect,
    Domain domain
) {
    private static final String DEFAULT_OAUTH_PAGE = "/oauth.html";

    // 성공 시 리다이렉트할 URL 생성
    public String getSuccessUrl(UUID uuid, OAuthParams params) {
        String domain = domain().getDomain(params.profile(), params.user());
        String uri = redirect().createSuccessUri(uuid);
        return domain + uri;
    }

    // 실패 시 리다이렉트할 URL 생성 - FailureHandler에서 사용할 메서드
    public String getErrorUrl(BbangleErrorCode code, OAuthParams params) {
        String domain = domain().getDomain(params.profile(), params.user());
        String uri = redirect().createErrorUri(code);
        return domain + uri;
    }

    // 실패 시 리다이렉트할 URL 생성 - OAuth2 URL 검증 Filter에서 사용할 메서드
    public String getErrorUrlWithReferer(BbangleErrorCode code, String referer) {
        String domain = domain().getDomain(referer);
        String uri = redirect().createErrorUri(code);

        if (domain == null)
            return getDefaultErrorUrl(code);
        return domain + uri;
    }

    // 실패 시 리다이렉트할 URL 생성 - 파라미터가 없어 리다이렉트를 하지 못할 경우 서버 내의 HTML 파일을 전송
    public String getDefaultErrorUrl(BbangleErrorCode code) {
        return DEFAULT_OAUTH_PAGE + "?error=" + code.toString() + "&code=" + code.getCode();
    }

    // OAuth2 성공, 실패 시 리다이렉트할 URI
    public record RedirectUrl(
        String success,
        String error
    ) {
        // OAuth2 로그인 성공 후 리다이렉트할 URI 생성하는 메서드
        private String createSuccessUri(UUID uuid) {
            return success + "?generateToken=" + uuid;
        }

        // OAuth2 로그인 실패 시 리다이렉트할 URI 생성하는 메서드
        private String createErrorUri(BbangleErrorCode code) {
            StringBuilder url = new StringBuilder(error);

            String error = code != null ? code.toString() : "UNKNOWN_ERROR";    // BbangleErrorCode 이외의 예외가 발생하면 UNKNOWN_ERROR 처리
            url.append("?error=").append(error);

            if (code != null) url.append("&code=").append(code.getCode());

            return url.toString();
        }
    }

    // 리다이렉트할 도메인을 화이트리스트로 설정
    public record Domain(
        String local,   // 로컬 도메인
        String seller,  // Seller 사이트 도메인
        String customer // Customer 사이트 도메인
    ) {
        // OAuth2 파라미터 값을 통해 도메인을 반환하는 메서드
        public String getDomain(ProfileType profile, UserType user) {
            return switch (profile) {
                case LOCAL -> local;    // profile 파라미터 값이 local인 경우
                case PROD -> switch (user) {
                    case CUSTOMER -> customer;  // profile 파라미터 값이 prod이고 user 파라미터 값이 customer인 경우
                    case SELLER -> seller;  // profile 파라미터 값이 prod이고 user 파라미터 값이 seller인 경우
                };
            };
        }

        // OAuth2 URL 검증 Filter에서 사용할 도메인 반환 메서드
        public String getDomain(String referer) {
            if (referer == null || referer.isBlank()) return null;
            if (referer.contains(local)) return local;  // Referer 헤더가 local인 경우
            if (referer.contains(customer)) return customer;    // Referer 헤더가 Seller 사이트 도메인인 경우
            if (referer.contains(seller)) return seller;    // Referer 헤더가 Customer 사이트 도메인인 경우
            return null;
        }
    }
}
