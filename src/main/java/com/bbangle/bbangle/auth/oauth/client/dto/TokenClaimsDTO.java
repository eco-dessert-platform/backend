package com.bbangle.bbangle.auth.oauth.client.dto;

import com.bbangle.bbangle.common.role.Role;
import lombok.Builder;

@Builder
public record TokenClaimsDTO(
    Long id,
    Role role
) {
}
