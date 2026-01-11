package com.bbangle.bbangle.auth.oauth.client.google.dto;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.auth.oauth.client.OAuth2Response;
import java.util.Map;

public class GoogleResponse implements OAuth2Response {

    private final Map<String, Object> attribute;

    public GoogleResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    @Override
    public OauthServerType getProvider() {
        return OauthServerType.GOOGLE;
    }

    @Override
    public String getProviderId() {
        return attribute.get("sub").toString();
    }

    @Override
    public String getEmail() {
        return getOptional(attribute, "email");
    }

    @Override
    public String getName() {
        return getOptional(attribute, "name");
    }

    @Override
    public String getNickname() {
        return getOptional(attribute, "given_name");
    }

    @Override
    public String getProfile() {
        return getOptional(attribute, "picture");
    }

    protected String getOptional(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }
}
