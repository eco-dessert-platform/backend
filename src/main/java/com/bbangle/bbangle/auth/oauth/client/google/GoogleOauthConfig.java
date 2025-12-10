package com.bbangle.bbangle.auth.oauth.client.google;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth2.google")
public record GoogleOauthConfig(
        String clientId,
        String redirectUri,
        String clientSecrete,
        String[] scope
) {

}
