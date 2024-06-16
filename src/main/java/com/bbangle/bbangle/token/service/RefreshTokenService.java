package com.bbangle.bbangle.token.service;

import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.token.domain.RefreshToken;
import com.bbangle.bbangle.common.redis.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
            .orElseThrow(() -> new BbangleException("Unexpected token"));
    }

}
