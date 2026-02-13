package com.bbangle.bbangle.auth.oauth.client.dto;

import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Builder
public record CustomUserDetails (
        Long id,
        Role role,
        String name,
        CertificationStatus status,
        OAuth2Redis.OAuthParams params
) implements OAuth2User {

    @Override
    public Map<String, Object> getAttributes() {
        return Collections.emptyMap();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(
                new SimpleGrantedAuthority(role.getRole())
        );
    }

    @Override
    public String getName() {
        return name;
    }

    public CertificationStatus getStatus() {
        return status;
    }
}
