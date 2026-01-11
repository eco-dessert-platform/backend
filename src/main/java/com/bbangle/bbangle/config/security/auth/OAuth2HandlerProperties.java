package com.bbangle.bbangle.config.security.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("oauth2.redirect")
public record OAuth2HandlerProperties (
    String success,
    String error
) {}
