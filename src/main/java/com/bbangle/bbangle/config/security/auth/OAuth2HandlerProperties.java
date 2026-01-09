package com.bbangle.bbangle.config.security.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties("oauth2.redirect")
public class OAuth2HandlerProperties {
    private String success;
    private String error;
}
