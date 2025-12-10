package com.bbangle.bbangle.auth.customer.service;

import com.bbangle.bbangle.auth.domain.RefreshToken;
import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.auth.oauth.client.OauthMemberClientComposite;
import com.bbangle.bbangle.auth.oauth.client.kakao.dto.LoginTokenResponse;
import com.bbangle.bbangle.common.redis.repository.RefreshTokenRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.member.customer.service.MemberService;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerOauthService {

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
        Long memberId = memberRepository.findByProviderAndProviderId(oauthMember.getProvider(),
                        oauthMember.getProviderId())
                .orElseGet(() -> memberService.getFirstJoinedMember(oauthMember).getId());

        String refreshToken = tokenProvider.generateToken(memberId, Role.ROLE_CUSTOMER, REFRESH_TOKEN_DURATION);
        String accessToken = tokenProvider.generateToken(memberId, Role.ROLE_CUSTOMER, ACCESS_TOKEN_DURATION);

        Optional<RefreshToken> refreshTokenByMemberId =
                refreshTokenRepository.findByUserIdAndUserRole(memberId, Role.ROLE_CUSTOMER);

        refreshTokenByMemberId.ifPresentOrElse(token -> refreshTokenByMemberId.get().update(refreshToken),
                () -> saveRefreshToken(refreshToken, memberId));

        return new LoginTokenResponse(accessToken, refreshToken);
    }

    private void saveRefreshToken(String refreshToken, Long memberId) {
        RefreshToken saveToken = RefreshToken.create(memberId, Role.ROLE_CUSTOMER, refreshToken);
        refreshTokenRepository.save(saveToken);
    }

}
