package com.bbangle.bbangle.token.oauth.infra.kakao.dto;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.token.oauth.domain.OauthServerType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;

@JsonNaming(SnakeCaseStrategy.class)
public record KakaoMemberResponse(
    String id,
    boolean hasSignedUp,
    LocalDateTime connectedAt,
    KakaoAccount kakaoAccount
) {

    public Member toMember() {
        return Member.builder()
            .providerId(this.id)
            .provider(OauthServerType.KAKAO)
            .nickname(kakaoAccount.profile.nickname)
            .profile(kakaoAccount.profile.profileImageUrl)
            .build();
    }

    @JsonNaming(SnakeCaseStrategy.class)
    private record KakaoAccount(
        boolean profileNeedsAgreement,
        boolean profileNicknameNeedsAgreement,
        boolean profileImageNeedsAgreement,
        Profile profile,
        boolean nameNeedsAgreement,
        String name,
        boolean emailNeedsAgreement,
        boolean isEmailValid,
        boolean isEmailVerified,
        String email,
        boolean ageRangeNeedsAgreement,
        String ageRange,
        boolean birthyearNeedsAgreement,
        String birthyear,
        boolean birthdayNeedsAgreement,
        String birthday,
        String birthdayType,
        boolean genderNeedsAgreement,
        String gender,
        boolean phoneNumberNeedsAgreement,
        String phoneNumber,
        boolean ciNeedsAgreement,
        String ci,
        LocalDateTime ciAuthenticatedAt
    ) {

    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record Profile(
        String nickname,
        String thumbnailImageUrl,
        String profileImageUrl,
        boolean isDefaultImage
    ) {

    }

}
