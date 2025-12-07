package com.bbangle.bbangle.auth.customer.service;

import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.member.customer.service.MemberService;
import com.bbangle.bbangle.member.domain.Member;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomerTokenService {

    private final TokenProvider tokenProvider;
    private final CustomerRefreshTokenService customerRefreshTokenService;
    private final MemberService memberService;

    public String createNewAccessToken(String refreshToken) {
        Long memberId = customerRefreshTokenService.findByRefreshToken(refreshToken)
                .getMemberId();
        Member member = memberService.findById(memberId);
        return tokenProvider.generateToken(member.getId(), member.getRole(), Duration.ofHours(2));
    }

}
