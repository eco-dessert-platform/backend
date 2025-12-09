package com.bbangle.bbangle.auth.admin.service;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.bbangle.bbangle.admin.domain.Admin;
import com.bbangle.bbangle.admin.repository.AdminRepository;
import com.bbangle.bbangle.auth.admin.dto.AdminLoginResponse;
import com.bbangle.bbangle.auth.admin.dto.AdminRequest.AdminLoginRequest;
import com.bbangle.bbangle.common.redis.repository.RefreshTokenRepository;
import com.bbangle.bbangle.common.role.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] AdminServiceIntegrationTest")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AdminAuthServiceIntegrationTest {

    @Autowired
    private AdminAuthService adminAuthService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    @DisplayName("로그인에 성공한다.")
    void login_success() {

        // arrange
        String rawPassword = "password";
        String encoded = passwordEncoder.encode(rawPassword);

        Admin admin = Admin.builder()
                .accountId("admin")
                .password(encoded) // 인코딩 진행한값
                .name("Admin")
                .build();

        adminRepository.saveAndFlush(admin);

        AdminLoginRequest request = new AdminLoginRequest("admin", rawPassword);
        // act
        AdminLoginResponse response = adminAuthService.login(request);

        // assert
        assertThat(adminRepository.findByAccountId("admin")).isPresent();
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getAccessToken()).isNotNull();
    }

    @Test
    @DisplayName("로그아웃에 성공한다")
    void logout_success() {
        // arrange
        String rawPassword = "password";
        String encoded = passwordEncoder.encode(rawPassword);

        Admin admin = Admin.builder()
                .accountId("admin")
                .password(encoded) // 인코딩 진행한값
                .name("Admin")
                .build();

        adminRepository.saveAndFlush(admin);

        Long adminId = admin.getId();
        AdminLoginRequest request = new AdminLoginRequest("admin", rawPassword);

        adminAuthService.login(request);

        // act
        adminAuthService.logout(adminId);

        // assert
        assertThat(refreshTokenRepository.findByUserIdAndUserRole(adminId, Role.ROLE_ADMIN)).isEmpty();
    }

}
