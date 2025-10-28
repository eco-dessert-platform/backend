package com.bbangle.bbangle.token.oauth;

import com.bbangle.bbangle.common.redis.repository.RefreshTokenRepository;
import com.bbangle.bbangle.member.customer.dto.MemberIdWithRoleDto;
import com.bbangle.bbangle.member.customer.service.MemberService;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.token.domain.RefreshToken;
import com.bbangle.bbangle.token.jwt.TokenProvider;
import com.bbangle.bbangle.token.oauth.domain.OauthServerType;
import com.bbangle.bbangle.token.oauth.domain.client.OauthMemberClientComposite;
import com.bbangle.bbangle.token.oauth.infra.kakao.dto.LoginTokenResponse;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OauthService {

    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(3);

    private final OauthMemberClientComposite oauthMemberClientComposite;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public LoginTokenResponse login(OauthServerType oauthServerType, String authCode) {
        Member oauthMember = oauthMemberClientComposite.fetch(oauthServerType, authCode);
        MemberIdWithRoleDto memberIdWithRoleDto = memberRepository.findByProviderAndProviderId(
                oauthMember.getProvider(),
                oauthMember.getProviderId())
            .orElseGet(
                () -> MemberIdWithRoleDto.from(memberService.getFirstJoinedMember(oauthMember)));

        String refreshToken = tokenProvider.generateToken(memberIdWithRoleDto.getMemberId(),
            memberIdWithRoleDto.getRole(),
            REFRESH_TOKEN_DURATION);
        String accessToken = tokenProvider.generateToken(memberIdWithRoleDto.getMemberId(),
            memberIdWithRoleDto.getRole(),
            ACCESS_TOKEN_DURATION);

        Optional<RefreshToken> refreshTokenByMemberId =
            refreshTokenRepository.findByMemberId(memberIdWithRoleDto.getMemberId());

        refreshTokenByMemberId.ifPresentOrElse(
            token -> refreshTokenByMemberId.get().update(refreshToken),
            () -> saveRefreshToken(refreshToken, memberIdWithRoleDto.getMemberId()));

        return new LoginTokenResponse(accessToken, refreshToken);
    }

    private void saveRefreshToken(String refreshToken, Long memberId) {
        RefreshToken saveToken = RefreshToken.builder()
            .refreshToken(refreshToken)
            .memberId(memberId)
            .build();

        refreshTokenRepository.save(saveToken);
    }

}
