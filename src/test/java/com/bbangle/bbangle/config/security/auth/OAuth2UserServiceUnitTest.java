package com.bbangle.bbangle.config.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.auth.oauth.client.dto.CustomUserDetails;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2Redis.OAuthParams;
import com.bbangle.bbangle.auth.seller.facade.OAuth2SellerFacade;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.seller.service.command.SellerCreateCommand;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

@DisplayName("[단위테스트] OAuth2UserService")
@ExtendWith(MockitoExtension.class)
class OAuth2UserServiceUnitTest {

    @Spy
    @InjectMocks
    OAuth2UserService oAuth2UserService;

    @Mock
    OAuth2SellerFacade oAuth2SellerFacade;

    @Mock
    OAuth2UserRequest request;

    @Mock
    ClientRegistration clientRegistration;

    // Kakao 용 User Info
    private OAuth2User kakaoOAuth2User() {
        Map<String, Object> properties = Map.of(
            "nickname", "test",
            "profile_image", "test.png"
        );
        Map<String, Object> attributes = Map.of(
            "id", 12345,
            "properties", properties
        );

        return new DefaultOAuth2User(
            List.of(() -> "OAUTH2_USER"),
            attributes,
            "id"
        );
    }

    private OAuth2User googleOAuth2User() {
        Map<String, Object> attributes = Map.of(
            "sub", 12345,
            "name", "test",
            "given_name", "temp",
            "picture", "test.png",
            "email", "test.com",
            "email_verified", true
        );
        // OAuth2 Server에서 조회한 User Info
        return new DefaultOAuth2User(
            List.of(() -> "OAUTH2_USER"),
            attributes,
            "sub"
        );
    }

    @Test
    @DisplayName("Kakao OAuth2 로그인 성공 시 CustomUserDetails를 반환한다.")
    void success_login_seller_kakao() {

        // given
        // OAuth2 Server에서 조회한 User Info
        OAuth2User oAuth2User = kakaoOAuth2User();
        OAuthParams params = new OAuthParams("seller", "test");
        doReturn(params).when(oAuth2UserService).getParams();

        Seller seller = Seller.create(
                "test",
                OauthServerType.KAKAO,
                "12345"
        );

        given(request.getClientRegistration()).willReturn(clientRegistration);
        given(clientRegistration.getRegistrationId()).willReturn("kakao");

        // super.loadUser() mocking
        doReturn(oAuth2User).when(oAuth2UserService).loadOAuth2User(any());

        given(oAuth2SellerFacade.login(any())).willReturn(seller);

        // when
        OAuth2User result = oAuth2UserService.loadUser(request);

        // then
        CustomUserDetails details = (CustomUserDetails) result;

        assertThat(details.role()).isEqualTo(Role.ROLE_SELLER);
        assertThat(details.name()).isEqualTo(seller.getName());
        assertThat(details.status()).isEqualTo(CertificationStatus.NEW);
        assertThat(details.params()).isEqualTo(params);

        verify(oAuth2SellerFacade).login(any(SellerCreateCommand.class));
    }

    @Test
    @DisplayName("Google OAuth2 로그인 성공 시 CustomUserDetails를 반환한다.")
    void success_login_seller_google() {

        // given
        // OAuth2 Server에서 조회한 User Info
        OAuth2User oAuth2User = googleOAuth2User();
        OAuthParams params = new OAuthParams("seller", "test");
        doReturn(params).when(oAuth2UserService).getParams();

        Seller seller = Seller.create(
                "test",
                OauthServerType.GOOGLE,
                "12345"
        );

        given(request.getClientRegistration()).willReturn(clientRegistration);
        given(clientRegistration.getRegistrationId()).willReturn("google");

        // super.loadUser() mocking
        doReturn(oAuth2User).when(oAuth2UserService).loadOAuth2User(any());

        given(oAuth2SellerFacade.login(any())).willReturn(seller);

        // when
        OAuth2User result = oAuth2UserService.loadUser(request);

        // then
        CustomUserDetails details = (CustomUserDetails) result;
        assertThat(details.role()).isEqualTo(Role.ROLE_SELLER);
        assertThat(details.name()).isEqualTo(seller.getName());
        assertThat(details.status()).isEqualTo(CertificationStatus.NEW);
        assertThat(details.params()).isEqualTo(params);

        verify(oAuth2SellerFacade).login(any(SellerCreateCommand.class));
    }

    @Test
    @DisplayName("OAuth2 인증도중 예외 발생 시 OAuth2Exception으로 변환한다.")
    void fail_login_seller_BbangleException() {

        // given
        // OAuth2 Server에서 조회한 User Info
        OAuth2User oAuth2User = kakaoOAuth2User();
        OAuthParams params = new OAuthParams("seller", "test");
        doReturn(params).when(oAuth2UserService).getParams();

        RuntimeException originalEx = new RuntimeException(BbangleErrorCode.INTERNAL_SERVER_ERROR.getMessage());

        given(request.getClientRegistration()).willReturn(clientRegistration);
        given(clientRegistration.getRegistrationId()).willReturn("kakao");

        doReturn(oAuth2User).when(oAuth2UserService).loadOAuth2User(any());

        given(oAuth2SellerFacade.login(any())).willThrow(originalEx);

        // when & then
        assertThatThrownBy(() -> oAuth2UserService.loadUser(request))
                .isInstanceOf(OAuth2Exception.class)
                .hasCause(originalEx)
                .satisfies(ex -> {
                    OAuth2Exception oAuth2Ex = (OAuth2Exception) ex;
                    assertThat(oAuth2Ex.getCode())
                            .isEqualTo(BbangleErrorCode.INTERNAL_SERVER_ERROR);
                });
    }

    @Test
    @DisplayName("지원하지 않는 OAuth2 Provider일 경우 예외를 던진다.")
    void fail_load_seller_UnsupportedProvider() {

        // given
        // OAuth2 Server에서 조회한 User Info
        OAuth2User oAuth2User = kakaoOAuth2User();
        OAuthParams params = new OAuthParams("seller", "test");
        doReturn(params).when(oAuth2UserService).getParams();

        given(request.getClientRegistration()).willReturn(clientRegistration);
        given(clientRegistration.getRegistrationId()).willReturn("facebook");

        doReturn(oAuth2User).when(oAuth2UserService).loadOAuth2User(any());

        // when & then
        assertThatThrownBy(() -> oAuth2UserService.loadUser(request))
                .isInstanceOf(OAuth2Exception.class)
                .satisfies(ex -> {
                    OAuth2Exception oAuth2Ex = (OAuth2Exception) ex;
                    assertThat(oAuth2Ex.getCode())
                            .isEqualTo(BbangleErrorCode.NOT_SUPPORTED_SERVER);
                });
    }

    @Test
    @DisplayName("Kakao에서 조회한 User의 name과 nickname이 전부 비공개일 경우 예외를 던진다.")
    void fail_login_name_nickname_null() {

        // given
        Map<String, Object> properties = Map.of(
            // name, nickname이 없음
            "profile_image", "test.png"
        );
        Map<String, Object> attributes = Map.of(
            "id", 12345,
            "properties", properties
        );
        OAuth2User oAuth2User = new DefaultOAuth2User(
            List.of(() -> "OAUTH2_USER"),
            attributes,
            "id"
        );
        OAuthParams params = new OAuthParams("seller", "test");
        doReturn(params).when(oAuth2UserService).getParams();

        given(request.getClientRegistration()).willReturn(clientRegistration);
        given(clientRegistration.getRegistrationId()).willReturn("kakao");

        doReturn(oAuth2User).when(oAuth2UserService).loadOAuth2User(any());

        // when & then
        assertThatThrownBy(() -> oAuth2UserService.loadUser(request))
            .isInstanceOf(OAuth2Exception.class)
            .satisfies(ex -> {
                OAuth2Exception oAuth2Ex = (OAuth2Exception) ex;
                assertThat(oAuth2Ex.getCode())
                    .isEqualTo(BbangleErrorCode.MISSING_NAME_NICKNAME);
            });

        verify(oAuth2SellerFacade, never()).login(any());
    }

    @Test
    @DisplayName("Google에서 조회한 User의 name과 given_name이 전부 비공개일 경우 예외를 던진다.")
    void fail_login_name_givenName_null() {

        // given
        Map<String, Object> attributes = Map.of(
            // name, given_name이 없음
            "sub", 12345,
            "picture", "test.png",
            "email", "test.com",
            "email_verified", true
        );
        // OAuth2 Server에서 조회한 User Info
        OAuth2User oAuth2User = new DefaultOAuth2User(
            List.of(() -> "OAUTH2_USER"),
            attributes,
            "sub"
        );
        OAuthParams params = new OAuthParams("seller", "test");
        doReturn(params).when(oAuth2UserService).getParams();

        given(request.getClientRegistration()).willReturn(clientRegistration);
        given(clientRegistration.getRegistrationId()).willReturn("google");

        doReturn(oAuth2User).when(oAuth2UserService).loadOAuth2User(any());

        // when & then
        assertThatThrownBy(() -> oAuth2UserService.loadUser(request))
            .isInstanceOf(OAuth2Exception.class)
            .satisfies(ex -> {
                OAuth2Exception oAuth2Ex = (OAuth2Exception) ex;
                assertThat(oAuth2Ex.getCode())
                    .isEqualTo(BbangleErrorCode.MISSING_NAME_NICKNAME);
            });

        verify(oAuth2SellerFacade, never()).login(any());
    }

    @Test
    @DisplayName("state에 해당하는 OAuthParams가 없으면 예외를 던진다.")
    void fail_when_oauth_params_null() {

        doThrow(new OAuth2Exception(BbangleErrorCode.OAUTH_INVALID_PARAMS)).when(oAuth2UserService).getParams();

        assertThatThrownBy(() -> oAuth2UserService.loadUser(request))
            .isInstanceOf(OAuth2Exception.class)
            .satisfies(ex -> {
                OAuth2Exception oAuth2Ex = (OAuth2Exception) ex;
                assertThat(oAuth2Ex.getCode())
                    .isEqualTo(BbangleErrorCode.OAUTH_INVALID_PARAMS);
            });

        verify(oAuth2SellerFacade, never()).login(any());
    }

    @Test
    @DisplayName("user 값이 seller/customer가 아니면 OAUTH_INVALID_PARAMS 예외를 던진다.")
    void fail_when_invalid_user_type() {

        OAuthParams params = new OAuthParams("admin", "test");
        doReturn(params).when(oAuth2UserService).getParams();

        OAuth2User oAuth2User = kakaoOAuth2User();

        given(request.getClientRegistration()).willReturn(clientRegistration);
        given(clientRegistration.getRegistrationId()).willReturn("kakao");

        doReturn(oAuth2User).when(oAuth2UserService).loadOAuth2User(any());

        assertThatThrownBy(() -> oAuth2UserService.loadUser(request))
            .isInstanceOf(OAuth2Exception.class)
            .satisfies(ex -> {
                OAuth2Exception oAuth2Ex = (OAuth2Exception) ex;
                assertThat(oAuth2Ex.getCode())
                    .isEqualTo(BbangleErrorCode.OAUTH_INVALID_PARAMS);
            });
    }
}