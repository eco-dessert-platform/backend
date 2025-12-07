package com.bbangle.bbangle.token.service;

import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.member.customer.service.MemberService;
import com.bbangle.bbangle.member.domain.Member;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TokenService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final MemberService memberService;

    public String createNewAccessToken(String refreshToken) {
        Long memberId = refreshTokenService.findByRefreshToken(refreshToken)
                .getMemberId();
        Member member = memberService.findById(memberId);
        return tokenProvider.generateToken(member.getId(), member.getRole(), Duration.ofHours(2));
    }

}
