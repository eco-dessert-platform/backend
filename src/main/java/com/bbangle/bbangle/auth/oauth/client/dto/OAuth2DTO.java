package com.bbangle.bbangle.auth.oauth.client.dto;

import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OAuth2DTO {

    public static String defaultProfile(String profile) {
        if (profile == null || profile.isBlank()) return ProfileType.PROD.name().toLowerCase();

        try {
            return ProfileType.valueOf(profile.toUpperCase())
                .name()
                .toLowerCase();
        } catch (IllegalArgumentException e) {
            return ProfileType.PROD.name().toLowerCase();
        }
    }

    public enum UserType {CUSTOMER, SELLER}
    public enum ProfileType {LOCAL, PROD}

    @Builder
    public record OAuthParams(
        UserType user,    // customer | seller
        ProfileType profile  // local | prod
    ) {}

    @Builder
    public record InfoDTO(
        Long id,
        Role role,
        CertificationStatus status
    ) {}
}