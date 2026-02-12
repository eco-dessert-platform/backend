package com.bbangle.bbangle.auth.oauth.client.dto;

import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import lombok.Builder;

public class OAuth2Redis {

    @Builder
    public record OAuthParams(
        String user,    // user | seller
        String profile  // local | prod
    ) {}

    @Builder
    public record InfoDTO(
        Long id,
        Role role,
        CertificationStatus status
    ) {}
}
