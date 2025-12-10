package com.bbangle.bbangle.auth.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.admin.domain.Admin;
import com.bbangle.bbangle.admin.repository.AdminRepository;
import com.bbangle.bbangle.auth.admin.dto.AdminLoginResponse;
import com.bbangle.bbangle.auth.admin.dto.AdminRequest;
import com.bbangle.bbangle.auth.admin.dto.AdminRequest.AdminLoginRequest;
import com.bbangle.bbangle.auth.domain.RefreshToken;
import com.bbangle.bbangle.common.redis.repository.RefreshTokenRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleException;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("[단위 테스트] AdminService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class AdminAuthServiceUnitTest {

    @InjectMocks
    private AdminAuthService adminAuthService;

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
        given(refreshTokenRepository.findByUserIdAndUserRole(any(), any())).willReturn(Optional.empty());

        // when
        AdminLoginResponse response = adminAuthService.login(request);

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
        assertThatThrownBy(() -> adminAuthService.login(request))
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
        assertThatThrownBy(() -> adminAuthService.login(request))
                .isInstanceOf(BbangleException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }


    @Test
    @DisplayName("로그아웃시 리프레시 토큰이 제거된다.")
    void logoutSuccess() {
        // given
        Long adminId = 1L;
        willDoNothing().given(refreshTokenRepository).deleteByUserIdAndUserRole(adminId, Role.ROLE_ADMIN);

        // when
        adminAuthService.logout(adminId);

        // then
        verify(refreshTokenRepository, times(1)).deleteByUserIdAndUserRole(adminId, Role.ROLE_ADMIN);
    }
}
