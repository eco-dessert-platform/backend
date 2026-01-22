package com.bbangle.bbangle.auth.oauth.client.dto;

import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import lombok.Builder;

@Builder
public record OAuth2InfoRedisDTO(
    Long id,
    Role role,
    CertificationStatus status
) {

}
