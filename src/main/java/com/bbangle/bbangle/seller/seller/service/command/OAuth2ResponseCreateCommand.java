package com.bbangle.bbangle.seller.seller.service.command;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import lombok.Builder;

public record OAuth2ResponseCreateCommand(
        String name,
        String nickname,
        OauthServerType provider,
        String providerId
) {

    @Builder
    public OAuth2ResponseCreateCommand(
            String name,
            String nickname,
            OauthServerType provider,
            String providerId
    ) {
        this.name = name;
        this.nickname = nickname;
        this.provider = provider;
        this.providerId = providerId;
    }
}
