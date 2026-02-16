package com.bbangle.bbangle.auth.oauth;

import static java.util.Locale.ENGLISH;

import com.bbangle.bbangle.auth.oauth.client.OAuth2Response;
import com.bbangle.bbangle.auth.oauth.client.google.dto.GoogleResponse;
import com.bbangle.bbangle.auth.oauth.client.kakao.dto.KakaoResponse;
import java.util.Map;

public enum OauthServerType {
    KAKAO {
        @Override
        public OAuth2Response create(Map<String, Object> attributes) {
            return new KakaoResponse(attributes);
        }
    },

    GOOGLE {
        @Override
        public OAuth2Response create(Map<String, Object> attributes) {
            return new GoogleResponse(attributes);
        }
    };

    public static OauthServerType fromName(String type) {
        return OauthServerType.valueOf(type.toUpperCase(ENGLISH));
    }

    public abstract OAuth2Response create(Map<String, Object> attributes);
}
