package com.bbangle.bbangle.admin.service;

import com.bbangle.bbangle.admin.domain.Admin;
import com.bbangle.bbangle.admin.admin.dto.AdminRequest;
import com.bbangle.bbangle.admin.admin.dto.AdminLoginResponse;
import com.bbangle.bbangle.admin.admin.dto.AdminRequest.AdminLoginRequest;
import com.bbangle.bbangle.admin.repository.AdminRepository;
import com.bbangle.bbangle.admin.admin.service.AdminService;
import com.bbangle.bbangle.common.redis.repository.RefreshTokenRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.token.domain.RefreshToken;
import com.bbangle.bbangle.token.jwt.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Optional;

import static com.bbangle.bbangle.admin.admin.service.AdminService.ACCESS_TOKEN_DURATION;
import static com.bbangle.bbangle.admin.admin.service.AdminService.REFRESH_TOKEN_DURATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminServiceUnitTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = Admin.builder()
                .accountId("admin")
                .password("encodedPassword")
                .name("Admin")
                .build();
        ReflectionTestUtils.setField(admin, "id", 1L);
    }

    @Test
    @DisplayName("로그인 성공 시 토큰을 반환한다")
    void loginSuccess() {
        // given
        AdminRequest.AdminLoginRequest request = new AdminLoginRequest("admin", "password");
        given(adminRepository.findByAccountId("admin")).willReturn(Optional.of(admin));
        given(passwordEncoder.matches("password", "encodedPassword")).willReturn(true);
        given(tokenProvider.generateToken(any(), any(), any(Duration.class))).willReturn("accessToken");
        given(refreshTokenRepository.findByAdminId(any())).willReturn(Optional.empty());

        // when
        AdminLoginResponse response = adminService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isNotNull();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("존재하지 않는 아이디로 로그인 시 예외가 발생한다")
    void loginFail_NotFound() {
        // given
        AdminLoginRequest request = new AdminLoginRequest("unknown", "password");
        given(adminRepository.findByAccountId("unknown")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.login(request))
                .isInstanceOf(BbangleException.class)
                .hasMessage("존재하지 않는 관리자입니다.");
    }


    @Test
    @DisplayName("비밀번호가 일치하지 않으면 예외가 발생한다")
    void loginFail_InvalidPassword() {
        // given
        AdminLoginRequest request = new AdminLoginRequest("admin", "wrongPassword");
        given(adminRepository.findByAccountId("admin")).willReturn(Optional.of(admin));
        given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);
        // when & then
        assertThatThrownBy(() -> adminService.login(request))
                .isInstanceOf(BbangleException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }
}
