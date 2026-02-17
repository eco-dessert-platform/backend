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

    public String getSuccessUrl(UUID uuid, OAuthParams params) {
        String domain = domain().getDomain(params.profile(), params.user());
        String uri = redirect().createSuccessUri(uuid);
        return domain + uri;
    }

    public String getErrorUrl(BbangleErrorCode code, OAuthParams params) {
        String domain = domain().getDomain(params.profile(), params.user());
        String uri = redirect().createErrorUri(code);
        return domain + uri;
    }

    public String getErrorUrlWithReferer(BbangleErrorCode code, String referer) {
        String domain = domain().getDomain(referer);
        String uri = redirect().createErrorUri(code);

        if (domain == null) return DEFAULT_OAUTH_PAGE + uri;
        return domain + uri;
    }

    public String getDefaultErrorUrl(BbangleErrorCode code) {
        String uri = redirect().createErrorUri(code);
        return DEFAULT_OAUTH_PAGE + uri;
    }

    public record RedirectUrl(
        String success,
        String error
    ) {
        private String createSuccessUri(UUID uuid) {
            return success + "?generateToken=" + uuid;
        }

        private String createErrorUri(BbangleErrorCode code) {
            StringBuilder url = new StringBuilder(error);

            String error = code != null ? code.toString() : "UNKNOWN_ERROR";
            url.append("?error=").append(error);

            if (code != null) url.append("&code=").append(code.getCode());

            return url.toString();
        }
    }

    public record Domain(
        String local,
        String seller,
        String customer
    ) {
        public String getDomain(ProfileType profile, UserType user) {
            return switch (profile) {
                case LOCAL -> local;
                case PROD -> switch (user) {
                    case CUSTOMER -> customer;
                    case SELLER -> seller;
                };
            };
        }

        public String getDomain(String referer) {
            if (referer == null || referer.isBlank()) return null;
            if (referer.contains(local)) return local;
            if (referer.contains(customer)) return customer;
            if (referer.contains(seller)) return seller;
            return null;
        }
    }
}
