package com.bbangle.bbangle.auth.oauth.client.dto;

import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import lombok.Builder;

public class OAuth2Redis {

    @Builder
    public record OAuthParams(
        String user,    // customer | seller
        String profile  // local | prod
    ) {
        public void validate() {
            if (!"customer".equals(user) && !"seller".equals(user)) {
                throw new OAuth2Exception(BbangleErrorCode.OAUTH_MISSING_PARAMS);
            }

            if (!"local".equals(profile) && !"prod".equals(profile)) {
                throw new OAuth2Exception(BbangleErrorCode.OAUTH_MISSING_PARAMS);
            }
        }
    }

    @Builder
    public record InfoDTO(
        Long id,
        Role role,
        CertificationStatus status
    ) {}
}
