package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2Redis.OAuthParams;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import java.util.UUID;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("oauth2")
public record OAuth2HandlerProperties (
    RedirectUrl redirect,
    Domain domain
) {
    public String getSuccessUrl(OAuthParams params, UUID uuid) {
        String domain = domain().getDomain(params);
        String uri = redirect().getSuccessUri(uuid);
        return domain + uri;
    }

    public String getErrorUrl(OAuthParams params, BbangleErrorCode code) {
        String domain = domain().getDomain(params);
        String uri = redirect().getErrorUri(code);
        return domain + uri;
    }

    public record RedirectUrl(
        String success,
        String error
    ) {
        private String getSuccessUri(UUID uuid) {
            return success + "?generateToken=" + uuid;
        }

        private String getErrorUri(BbangleErrorCode code) {
            if (code == null) {
                return error + "?error=" + "UNKNOWN_ERROR";
            } else {
                return error + "?error=" + "?error=" + code + "&code=" + code.getCode();
            }
        }
    }

    public record Domain(
        String local,
        String seller,
        String customer
    ) {
        private String getDomain(OAuthParams params) {
            return switch (params.profile()) {
                case "local" -> local;
                case "prod" -> switch (params.user()) {
                    case "seller" -> seller;
                    case "customer" -> customer;
                    default -> throw new OAuth2Exception(BbangleErrorCode.OAUTH_MISSING_PARAMS);
                };

                default -> throw new OAuth2Exception(BbangleErrorCode.OAUTH_MISSING_PARAMS);
            };
        }
    }
}
