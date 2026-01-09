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
        return attribute.get("email").toString();
    }

    @Override
    public String getName() {
        return attribute.get("name").toString();
    }

    @Override
    public String getNickname() {
        return attribute.get("given_name").toString();
    }

    @Override
    public String getProfile() {
        return attribute.get("picture").toString();
    }
}
