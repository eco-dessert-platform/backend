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
import com.bbangle.bbangle.auth.admin.dto.AdminReissueResponse;
import com.bbangle.bbangle.auth.admin.dto.AdminRequest;
import com.bbangle.bbangle.auth.admin.dto.AdminRequest.AdminLoginRequest;
import com.bbangle.bbangle.auth.admin.dto.AdminRequest.AdminReissueRequest;
import com.bbangle.bbangle.auth.domain.RefreshToken;
import com.bbangle.bbangle.auth.oauth.client.dto.TokenClaimsDTO;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    @Test
    @DisplayName("관리자 비밀번호 생성 테스트")
    void getEncodedPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "test1234"; // 사용하고 싶은 실제 비밀번호
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("Encoded Password: " + encodedPassword);
    }

    @Test
    @DisplayName("reissue 성공 시 새 토큰을 반환하고 RefreshToken을 갱신한다")
    void reissueSuccess() {
        // given
        String oldRefreshToken = "oldRefreshToken";
        RefreshToken storedToken = RefreshToken.create(1L, Role.ROLE_ADMIN, oldRefreshToken);
        TokenClaimsDTO claims = TokenClaimsDTO.builder()
            .id(1L)
            .role(Role.ROLE_ADMIN)
            .build();

        given(tokenProvider.isValidToken(oldRefreshToken)).willReturn(true);
        given(tokenProvider.parseRefreshToken(oldRefreshToken)).willReturn(claims);
        given(refreshTokenRepository.findByRefreshToken(oldRefreshToken)).willReturn(Optional.of(storedToken));
        given(tokenProvider.generateToken(1L, Role.ROLE_ADMIN, AdminAuthService.ACCESS_TOKEN_DURATION))
            .willReturn("newAccessToken");
        given(tokenProvider.generateToken(1L, Role.ROLE_ADMIN, AdminAuthService.REFRESH_TOKEN_DURATION))
            .willReturn("newRefreshToken");

        // when
        AdminReissueResponse response = adminAuthService.reissue(new AdminReissueRequest(oldRefreshToken));

        // then
        assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("newRefreshToken");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("reissue 시 유효하지 않은 토큰이면 예외가 발생한다")
    void reissueFail_invalidToken() {
        // given
        String invalidToken = "invalidToken";
        given(tokenProvider.isValidToken(invalidToken)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> adminAuthService.reissue(new AdminReissueRequest(invalidToken)))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining("유효하지 않은 리프레시 토큰입니다.");
    }

    @Test
    @DisplayName("reissue 시 role이 ROLE_ADMIN이 아니면 예외가 발생한다")
    void reissueFail_notAdminRole() {
        // given
        String customerRefreshToken = "customerRefreshToken";
        TokenClaimsDTO claims = TokenClaimsDTO.builder()
            .id(1L)
            .role(Role.ROLE_CUSTOMER)
            .build();

        given(tokenProvider.isValidToken(customerRefreshToken)).willReturn(true);
        given(tokenProvider.parseRefreshToken(customerRefreshToken)).willReturn(claims);

        // when & then
        assertThatThrownBy(() -> adminAuthService.reissue(new AdminReissueRequest(customerRefreshToken)))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining("유효하지 않은 리프레시 토큰입니다.");
    }

    @Test
    @DisplayName("reissue 시 DB에 refresh가 존재하지 않으면 예외가 발생한다")
    void reissueFail_notFoundInDb() {
        // given
        String unknownRefreshToken = "unknownRefreshToken";
        TokenClaimsDTO claims = TokenClaimsDTO.builder()
            .id(1L)
            .role(Role.ROLE_ADMIN)
            .build();

        given(tokenProvider.isValidToken(unknownRefreshToken)).willReturn(true);
        given(tokenProvider.parseRefreshToken(unknownRefreshToken)).willReturn(claims);
        given(refreshTokenRepository.findByRefreshToken(unknownRefreshToken)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminAuthService.reissue(new AdminReissueRequest(unknownRefreshToken)))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining("유효하지 않은 리프레시 토큰입니다.");
    }
}
