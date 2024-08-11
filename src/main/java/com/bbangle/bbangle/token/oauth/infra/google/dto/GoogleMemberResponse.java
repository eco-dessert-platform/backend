package com.bbangle.bbangle.token.oauth.infra.google.dto;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.token.oauth.domain.OauthServerType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;

@JsonNaming(SnakeCaseStrategy.class)
public record GoogleMemberResponse(
    String id,
    boolean hasSignedUp,
    LocalDateTime connectedAt,
    String email,
    String name,
    String givenName,
    String familyName,
    String picture
) {
    public Member toMember(){
        return Member.builder()
                .providerId(id)
                .provider(OauthServerType.GOOGLE)
                //FIXME 임시(팀원들과 논의 필요)
                .nickname(name)
                .email(email)
                .build();
    }
}
