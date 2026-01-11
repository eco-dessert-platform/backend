package com.bbangle.bbangle.auth.oauth.client.kakao.dto;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.auth.oauth.client.OAuth2Response;
import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attributes;
    private final Map<String, Object> properties;

    public KakaoResponse(Map<String, Object> attribute) {

        this.attributes = attribute;
        this.properties = (Map<String, Object>) attribute.get("properties");
    }

    @Override
    public OauthServerType getProvider() {
        return OauthServerType.KAKAO;
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public String getName() {
        return getOptional(properties, "name");
    }

    @Override
    public String getNickname() {
        return getOptional(properties, "nickname");
    }

    @Override
    public String getProfile() {
        return getOptional(properties, "profile_image");
    }

    protected String getOptional(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }
}
