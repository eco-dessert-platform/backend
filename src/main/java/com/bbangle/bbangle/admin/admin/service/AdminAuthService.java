package com.bbangle.bbangle.admin.admin.service;

import com.bbangle.bbangle.admin.admin.dto.AdminLoginResponse;
import com.bbangle.bbangle.admin.admin.dto.AdminRequest;
import com.bbangle.bbangle.admin.domain.Admin;
import com.bbangle.bbangle.admin.repository.AdminRepository;
import com.bbangle.bbangle.common.redis.repository.RefreshTokenRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.token.domain.RefreshToken;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthService {

    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(10);

    private final AdminRepository adminRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AdminLoginResponse login(AdminRequest.AdminLoginRequest request) {
        Admin admin = adminRepository.findByAccountId(request.accountId())
                .orElseThrow(() -> new BbangleException(BbangleErrorCode.ADMIN_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new BbangleException(BbangleErrorCode.ADMIN_INVALID_PASSWORD);
        }

        String accessToken = tokenProvider.generateToken(admin.getId(), Role.ROLE_ADMIN,
                ACCESS_TOKEN_DURATION);
        String refreshTokenVal = tokenProvider.generateToken(admin.getId(), Role.ROLE_ADMIN,
                REFRESH_TOKEN_DURATION);

        RefreshToken refreshToken = refreshTokenRepository.findByAdminId(admin.getId())
                .orElse(RefreshToken.builder()
                        .adminId(admin.getId())
                        .refreshToken(refreshTokenVal)
                        .build());

        // upsert 방식
        // 매 로그인마다 refresh token 갱신
        refreshToken.update(refreshTokenVal);
        refreshTokenRepository.save(refreshToken);

        return AdminLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenVal)
                .build();
    }

    @Transactional
    public void logout(Long adminId) {
        int deletedCount = refreshTokenRepository.deleteByAdminId(adminId);
        if (deletedCount == 0) {
            throw new BbangleException(BbangleErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
    }
}
