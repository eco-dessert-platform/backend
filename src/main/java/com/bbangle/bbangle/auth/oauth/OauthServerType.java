package com.bbangle.bbangle.auth.oauth;

import static java.util.Locale.ENGLISH;

public enum OauthServerType {
    KAKAO,
    GOOGLE;

    public static OauthServerType fromName(String type) {
        return OauthServerType.valueOf(type.toUpperCase(ENGLISH));
    }
}
