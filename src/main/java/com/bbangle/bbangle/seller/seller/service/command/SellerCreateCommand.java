package com.bbangle.bbangle.seller.seller.service.command;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.auth.oauth.client.OAuth2Response;
import lombok.Builder;

public record SellerCreateCommand(
        String name,
        String nickname,
        OauthServerType provider,
        String providerId
) {

    @Builder
    public SellerCreateCommand(
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

    public static SellerCreateCommand from(OAuth2Response response) {
        return SellerCreateCommand.builder()
                .name(response.getName())
                .nickname(response.getNickname())
                .provider(response.getProvider())
                .providerId(response.getProviderId())
                .build();
    }

    public String resolvedName() {
        return name != null ? name : nickname;
    }
}
