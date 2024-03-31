package com.bbangle.bbangle.token.oauth.domain;

import static java.util.Locale.ENGLISH;

public enum OauthServerType {
    KAKAO,
    GOOGLE;

    public static OauthServerType fromName(String type) {
        return OauthServerType.valueOf(type.toUpperCase(ENGLISH));
    }
}
