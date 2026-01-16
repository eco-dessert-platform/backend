package com.bbangle.bbangle.auth.seller.service;

import com.bbangle.bbangle.auth.domain.RefreshToken;
import com.bbangle.bbangle.auth.seller.service.dto.SellerInfoRedisDTO;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.common.redis.repository.RefreshTokenRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO : Test
@Service
@RequiredArgsConstructor
public class OAuthSellerService {

    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(3);

    private final RedisRepository redisRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public SellerInfoRedisDTO getSellerInfoFromRedis(String code) {
        Map<Object, Object> sellerInfo = redisRepository.getMap("oauth2:code", code);
        if (sellerInfo == null || sellerInfo.isEmpty()) throw new BbangleException(BbangleErrorCode.UNAUTHORIZED);

        redisRepository.delete("oauth2:code", code);

        return SellerInfoRedisDTO.fromMap(sellerInfo);
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
        // UNIQUE 충돌을 방지하기 위해 Redis 분산 락?
        refreshTokenRepository.save(token);

        return refreshToken;
    }

    public String generateAccessToken(Long sellerId, Role role) {
        return tokenProvider.generateToken(sellerId, role, ACCESS_TOKEN_DURATION);
    }
}
