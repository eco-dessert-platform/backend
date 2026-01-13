package com.bbangle.bbangle.exception;

import lombok.Getter;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

@Getter
public class OAuth2Exception extends OAuth2AuthenticationException {

    private final BbangleErrorCode code;

    public OAuth2Exception(BbangleErrorCode code, Throwable cause) {
        super(
                new OAuth2Error(code.name(), code.getMessage(), null),
                cause
        );

        this.code = code;
    }

    public OAuth2Exception(BbangleErrorCode code) {
        super(
                new OAuth2Error(code.name(), code.getMessage(), null)
        );

        this.code = code;
    }
}
