package com.bbangle.bbangle.config.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.auth.oauth.client.dto.CustomUserDetails;
import com.bbangle.bbangle.auth.seller.facade.OAuth2SellerFacade;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import com.bbangle.bbangle.seller.domain.OAuth2Seller;
import com.bbangle.bbangle.seller.seller.service.command.OAuth2ResponseCreateCommand;
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

    @Test
    @DisplayName("Kakao OAuth2 로그인 성공 시 CustomUserDetails를 반환한다.")
    void success_login_seller() {

        // given
        Map<String, Object> properties = Map.of(
                "nickname", "test",
                "profile_image", "test.png"
        );
        Map<String, Object> attributes = Map.of(
                "id", 12345,
                "properties", properties
        );
        // OAuth2 Server에서 조회한 User Info
        OAuth2User oAuth2User = new DefaultOAuth2User(
                List.of(() -> "OAUTH2_USER"),
                attributes,
                "id"
        );

        OAuth2Seller seller = OAuth2Seller.create(
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
        assertThat(result).isInstanceOf(CustomUserDetails.class);

        CustomUserDetails details = (CustomUserDetails) result;
        assertThat(details.role()).isEqualTo(Role.ROLE_SELLER);
        assertThat(details.name()).isEqualTo(seller.getName());

        verify(oAuth2SellerFacade).login(any(OAuth2ResponseCreateCommand.class));
    }

    @Test
    @DisplayName("OAuth2 인증도중 예외 발생 시 OAuth2Exception으로 변환한다.")
    void fail_login_seller_BbangleException() {

        // given
        Map<String, Object> properties = Map.of(
                "nickname", "test",
                "profile_image", "test.png"
        );
        Map<String, Object> attributes = Map.of(
                "id", 12345,
                "properties", properties
        );
        // OAuth2 Server에서 조회한 User Info
        OAuth2User oAuth2User = new DefaultOAuth2User(
                List.of(() -> "OAUTH2_USER"),
                attributes,
                "id"
        );

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
        Map<String, Object> properties = Map.of(
                "nickname", "test",
                "profile_image", "test.png"
        );
        Map<String, Object> attributes = Map.of(
                "id", 12345,
                "properties", properties
        );
        // OAuth2 Server에서 조회한 User Info
        OAuth2User oAuth2User = new DefaultOAuth2User(
                List.of(() -> "OAUTH2_USER"),
                attributes,
                "id"
        );

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
}