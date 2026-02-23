package com.bbangle.bbangle.auth.seller.service;

import com.bbangle.bbangle.auth.domain.RefreshToken;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.common.redis.repository.RefreshTokenRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthSellerService {

    public static final String OAUTH_CODE_NAMESPACE = "oauth2:code";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(3);

    private final RedisRepository redisRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public OAuth2DTO.InfoDTO getSellerInfoFromRedis(String code) {
        OAuth2DTO.InfoDTO sellerInfo;
        try {
            sellerInfo = redisRepository.getDTOAndDelete(OAUTH_CODE_NAMESPACE, code, OAuth2DTO.InfoDTO.class);
        } catch (Exception e) {
            log.warn("Redis 조회/삭제 실패 : ", e);
            throw new BbangleException(BbangleErrorCode._UNAUTHORIZED);
        }

        if (sellerInfo == null) throw new BbangleException(BbangleErrorCode._UNAUTHORIZED);

        return sellerInfo;
    }

    @Transactional
    public String generateRefreshToken(Long sellerId, Role role) {
        String refreshToken = tokenProvider.generateToken(sellerId, role, REFRESH_TOKEN_DURATION);

        RefreshToken token = refreshTokenRepository
            .findByUserIdAndUserRole(sellerId, role)
            .orElseGet(() -> RefreshToken.create(
                sellerId,
                role,
                refreshToken
            ));

        token.update(refreshToken);

        refreshTokenRepository.save(token);

        return refreshToken;
    }

    public String generateAccessToken(Long sellerId, Role role) {
        return tokenProvider.generateToken(sellerId, role, ACCESS_TOKEN_DURATION);
    }

    public void refreshTokenValidate(String refreshToken) {
        refreshTokenRepository.findByRefreshToken(refreshToken)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode._UNAUTHORIZED));
    }

    @Transactional
    public void deleteRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteByRefreshToken(refreshToken);
    }
}
