package com.bbangle.bbangle.auth.customer.service;

import com.bbangle.bbangle.auth.domain.RefreshToken;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
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
        RefreshToken refreshTokenEntity = customerRefreshTokenService.findByRefreshToken(refreshToken);
        if (!refreshTokenEntity.isCustomer()) {
            throw new BbangleException(BbangleErrorCode.INVALID_REFRESH_TOKEN);
        }
        Member member = memberService.findById(refreshTokenEntity.getId());
        return tokenProvider.generateToken(member.getId(), Role.ROLE_CUSTOMER, Duration.ofHours(2));
    }

}
