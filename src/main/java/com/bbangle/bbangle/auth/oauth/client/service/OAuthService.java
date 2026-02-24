package com.bbangle.bbangle.auth.oauth.client.service;

import com.bbangle.bbangle.auth.domain.RefreshToken;
import com.bbangle.bbangle.common.redis.repository.RefreshTokenRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public String generateRefreshToken(Long userId, Role role) {
        String refreshToken = tokenProvider.generateToken(userId, role, TokenProvider.REFRESH_TOKEN_DURATION);

        refreshTokenRepository.findByUserIdAndUserRole(userId, role)
            .ifPresentOrElse(
                existing -> existing.update(refreshToken),
                () -> refreshTokenRepository.save(
                    RefreshToken.create(userId, role, refreshToken)
                )
            );

        return refreshToken;
    }

    public String generateAccessToken(Long userId, Role role) {
        return tokenProvider.generateToken(userId, role, TokenProvider.ACCESS_TOKEN_DURATION);
    }

    @Transactional(readOnly = true)
    public void refreshTokenValidate(String refreshToken) {
        refreshTokenRepository.findByRefreshToken(refreshToken)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode._UNAUTHORIZED));
    }

    @Transactional
    public void deleteRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteByRefreshToken(refreshToken);
    }
}
