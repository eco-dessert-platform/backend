package com.bbangle.bbangle.auth.seller.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO;
import com.bbangle.bbangle.auth.oauth.client.dto.TokenClaimsDTO;
import com.bbangle.bbangle.auth.oauth.client.dto.TokenResponse;
import com.bbangle.bbangle.auth.seller.facade.dto.GenerateTokenDTO;
import com.bbangle.bbangle.auth.seller.service.OAuthSellerService;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.seller.service.SellerService;
import com.bbangle.bbangle.seller.seller.service.command.SellerCreateCommand;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@DisplayName("[단위테스트] OAuth2SellerFacade")
@ExtendWith(MockitoExtension.class)
class OAuth2SellerFacadeUnitTest {

    private final OauthServerType provider = OauthServerType.KAKAO;
    private final String providerId = "12345";

    @InjectMocks
    private OAuth2SellerFacade oAuth2SellerFacade;

    @Mock
    private SellerService sellerService;

    @Mock
    private OAuthSellerService oAuthSellerService;

    @Mock
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("판매자 계정이 없으면 새로 생성한다.")
    void success_login_seller_create() {

        // given
        SellerCreateCommand command =
                SellerCreateCommand.builder()
                        .provider(provider)
                        .providerId(providerId)
                        .name("test")
                        .nickname("test")
                        .build();

        Seller seller = Seller.create("test", provider, providerId);

        given(sellerService.findByProviderAndProviderId(provider, providerId))
                .willReturn(Optional.empty());  // 테이블에 판매자 계정이 존재하지 않는다고 가정

        given(sellerService.createOAuth2Seller(command)).willReturn(seller);    // 판매자 계정을 생성한다고 가정

        // when
        Seller result = oAuth2SellerFacade.login(command);

        // then
        assertThat(result).isEqualTo(seller);

        verify(sellerService).findByProviderAndProviderId(provider, providerId);
        verify(sellerService).createOAuth2Seller(any(SellerCreateCommand.class));
    }

    @Test
    @DisplayName("판매자 계정이 존재할 경우 생성하지 않고 반환한다.")
    void success_login_seller_exist() {

        // given
        SellerCreateCommand command =
                SellerCreateCommand.builder()
                        .provider(provider)
                        .providerId(providerId)
                        .name("test")
                        .nickname("test")
                        .build();

        Seller seller = Seller.create("test", provider, providerId);

        given(sellerService.findByProviderAndProviderId(provider, providerId))
                .willReturn(Optional.of(seller));

        // when
        Seller result = oAuth2SellerFacade.login(command);

        // then
        assertThat(result).isEqualTo(seller);

        verify(sellerService).findByProviderAndProviderId(provider, providerId);
        verify(sellerService, never()).createOAuth2Seller(any()); // 판매자 계정 생성 메서드가 호출되지 않았는지 확인
    }

    @Test
    @DisplayName("동시에 OAuth2 로그인을 시도할 경우 UNIQUE 충돌 시 재조회 후 반환한다.")
    void login_seller_unique_constraint_violation() {

        // given
        SellerCreateCommand command =
                SellerCreateCommand.builder()
                        .provider(provider)
                        .providerId(providerId)
                        .name("test")
                        .nickname("test")
                        .build();

        Seller seller = Seller.create("test", provider, providerId);

        given(sellerService.findByProviderAndProviderId(provider, providerId))
                .willReturn(Optional.empty())    // 최초 조회 시 판매자 계정이 없다고 가정
                .willReturn(Optional.of(seller));   // 두번째 조회 시 판매자 계정이 존재한다고 가정

        given(sellerService.createOAuth2Seller(command))
                .willThrow(DataIntegrityViolationException.class);  // 판매자 계정 생성 시 UNIQUE 충돌이 일어났다고 가정

        // when
        Seller result = oAuth2SellerFacade.login(command);

        // then
        assertThat(result).isEqualTo(seller);

        // 첫번째 조회 -> 판매자 계정 없음 -> 판매자 계정 생성 -> UNIQUE 충돌 -> 판매자 재조회
        InOrder inOrder = inOrder(sellerService);
        inOrder.verify(sellerService).findByProviderAndProviderId(provider, providerId);
        inOrder.verify(sellerService).createOAuth2Seller(any());
        inOrder.verify(sellerService).findByProviderAndProviderId(provider, providerId);
    }

    @Test
    @DisplayName("Redis의 Seller 정보 기반으로 JWT를 생성한다.")
    void success_generate_Token() {

        // given
        String code = "authCode";
        OAuth2DTO.InfoDTO sellerInfo = OAuth2DTO.InfoDTO.builder()
            .id(1L)
            .role(Role.ROLE_SELLER)
            .status(CertificationStatus.NEW)
            .build();

        given(oAuthSellerService.getSellerInfoFromRedis(code)).willReturn(sellerInfo);
        given(oAuthSellerService.generateRefreshToken(1L, Role.ROLE_SELLER)).willReturn("refreshToken");
        given(oAuthSellerService.generateAccessToken(1L, Role.ROLE_SELLER)).willReturn("accessToken");

        // when
        GenerateTokenDTO response = oAuth2SellerFacade.generateToken(code);

        // then
        assertThat(response.refreshToken()).isEqualTo("refreshToken");
        assertThat(response.accessToken()).isEqualTo("accessToken");
        assertThat(response.sellerId()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(CertificationStatus.NEW);

        verify(oAuthSellerService).getSellerInfoFromRedis(code);
        verify(oAuthSellerService).generateRefreshToken(1L, Role.ROLE_SELLER);
        verify(oAuthSellerService).generateAccessToken(1L, Role.ROLE_SELLER);
    }

    @Test
    @DisplayName("Redis에 Seller 정보가 없으면 예외를 던진다.")
    void failure_generate_Token() {

        // given
        given(oAuthSellerService.getSellerInfoFromRedis("code"))
            .willThrow(new BbangleException(BbangleErrorCode._UNAUTHORIZED));

        // when & then
        assertThatThrownBy(() -> oAuth2SellerFacade.generateToken("code"))
            .isInstanceOf(BbangleException.class);
    }

    @Test
    @DisplayName("Refresh Token이 유효하면 토큰을 재발급한다.")
    void success_reissueToken() {

        // given
        String refreshToken = "validRefreshToken";
        TokenClaimsDTO tokenClaimsDTO = TokenClaimsDTO.builder()
            .id(1L)
            .role(Role.ROLE_SELLER)
            .build();

        given(tokenProvider.parseRefreshToken(refreshToken)).willReturn(tokenClaimsDTO);

        willDoNothing().given(oAuthSellerService).refreshTokenValidate(refreshToken);
        willDoNothing().given(oAuthSellerService).deleteRefreshToken(refreshToken);

        given(oAuthSellerService.generateRefreshToken(1L, Role.ROLE_SELLER)).willReturn("newRefreshToken");
        given(oAuthSellerService.generateAccessToken(1L, Role.ROLE_SELLER)).willReturn("newAccessToken");

        // when
        TokenResponse response = oAuth2SellerFacade.reissueToken(refreshToken);

        // then
        assertThat(response.refreshToken()).isEqualTo("newRefreshToken");
        assertThat(response.accessToken()).isEqualTo("newAccessToken");

        InOrder inOrder = inOrder(tokenProvider, oAuthSellerService);
        inOrder.verify(tokenProvider).parseRefreshToken(refreshToken);
        inOrder.verify(oAuthSellerService).refreshTokenValidate(refreshToken);
        inOrder.verify(oAuthSellerService).deleteRefreshToken(refreshToken);
        inOrder.verify(oAuthSellerService).generateRefreshToken(1L, Role.ROLE_SELLER);
        inOrder.verify(oAuthSellerService).generateAccessToken(1L, Role.ROLE_SELLER);
    }

    @Test
    @DisplayName("Refresh Token이 만료되면 예외 발생")
    void failure_reissueToken_expired() {

        // given
        String refreshToken = "expiredRefreshToken";

        given(tokenProvider.parseRefreshToken(refreshToken)).willThrow(new BbangleException(BbangleErrorCode._UNAUTHORIZED));

        // when & then
        assertThatThrownBy(() -> oAuth2SellerFacade.reissueToken(refreshToken))
            .isInstanceOf(BbangleException.class)
            .satisfies(e -> {
                BbangleException ex = (BbangleException) e;
                assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode._UNAUTHORIZED);
            });

        verify(oAuthSellerService, never()).refreshTokenValidate(refreshToken);
        verify(oAuthSellerService, never()).deleteRefreshToken(any());
        verify(oAuthSellerService, never()).generateRefreshToken(any(), any());
        verify(oAuthSellerService, never()).generateAccessToken(any(), any());
    }

    @Test
    @DisplayName("Refresh Token이 DB에 없으면 예외 발생")
    void failure_reissueToken_notExist() {

        // given
        String refreshToken = "invalidRefreshToken";
        TokenClaimsDTO tokenClaimsDTO = TokenClaimsDTO.builder()
            .id(1L)
            .role(Role.ROLE_SELLER)
            .build();

        given(tokenProvider.parseRefreshToken(refreshToken)).willReturn(tokenClaimsDTO);
        willThrow(new BbangleException(BbangleErrorCode._UNAUTHORIZED)).given(oAuthSellerService).refreshTokenValidate(refreshToken);

        // when & then
        assertThatThrownBy(() -> oAuth2SellerFacade.reissueToken(refreshToken))
            .isInstanceOf(BbangleException.class)
            .satisfies(e -> {
                BbangleException ex = (BbangleException) e;
                assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode._UNAUTHORIZED);
            });

        verify(oAuthSellerService, never()).deleteRefreshToken(any());
        verify(oAuthSellerService, never()).generateRefreshToken(any(), any());
        verify(oAuthSellerService, never()).generateAccessToken(any(), any());
    }
}