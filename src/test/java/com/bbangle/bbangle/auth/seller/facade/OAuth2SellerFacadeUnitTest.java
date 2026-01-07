package com.bbangle.bbangle.auth.seller.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.seller.domain.OAuth2Seller;
import com.bbangle.bbangle.seller.seller.service.OAuth2SellerService;
import com.bbangle.bbangle.seller.seller.service.command.OAuth2ResponseCreateCommand;
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
    private OAuth2SellerService oAuth2SellerService;

    @Test
    @DisplayName("판매자 계정이 없으면 새로 생성한다.")
    void success_login_seller_create() {

        // given
        OAuth2ResponseCreateCommand command =
                OAuth2ResponseCreateCommand.builder()
                        .provider(provider)
                        .providerId(providerId)
                        .name("test")
                        .nickname("test")
                        .build();

        OAuth2Seller seller = OAuth2Seller.create("test", provider, providerId);

        given(oAuth2SellerService.findByProviderAndProviderId(provider, providerId))
                .willReturn(Optional.empty());  // 테이블에 판매자 계정이 존재하지 않는다고 가정

        given(oAuth2SellerService.createOAuth2Seller(command)).willReturn(seller);    // 판매자 게정을 생성한다고 가정

        // when
        OAuth2Seller result = oAuth2SellerFacade.login(command);

        // then
        assertThat(result).isEqualTo(seller);

        verify(oAuth2SellerService).findByProviderAndProviderId(provider, providerId);
        verify(oAuth2SellerService).createOAuth2Seller(any(OAuth2ResponseCreateCommand.class));
    }

    @Test
    @DisplayName("판매자 계정이 존재할 경우 생성하지 않고 반환한다.")
    void success_login_seller_exist() {

        // given
        OAuth2ResponseCreateCommand command =
                OAuth2ResponseCreateCommand.builder()
                        .provider(provider)
                        .providerId(providerId)
                        .name("test")
                        .nickname("test")
                        .build();

        OAuth2Seller seller = OAuth2Seller.create("test", provider, providerId);

        given(oAuth2SellerService.findByProviderAndProviderId(provider, providerId))
                .willReturn(Optional.of(seller));

        // when
        OAuth2Seller result = oAuth2SellerFacade.login(command);

        // then
        assertThat(result).isEqualTo(seller);

        verify(oAuth2SellerService).findByProviderAndProviderId(provider, providerId);
        verify(oAuth2SellerService, never()).createOAuth2Seller(any()); // 판매자 게정 생성 메서드가 호출되지 않았는지 확인
    }

    @Test
    @DisplayName("동시에 OAuth2 로그인을 시도할 경우 UNIQUE 충돌 시 재조회 후 반환한다.")
    void login_seller_unique_constraint_violation() {

        // given
        OAuth2ResponseCreateCommand command =
                OAuth2ResponseCreateCommand.builder()
                        .provider(provider)
                        .providerId(providerId)
                        .name("test")
                        .nickname("test")
                        .build();

        OAuth2Seller seller = OAuth2Seller.create("test", provider, providerId);

        given(oAuth2SellerService.findByProviderAndProviderId(provider, providerId))
                .willReturn(Optional.empty())    // 최초 조회 시 판매자 계정이 없다고 가정
                .willReturn(Optional.of(seller));   // 두번째 조회 시 판매자 계정이 존재한다고 가정

        given(oAuth2SellerService.createOAuth2Seller(command))
                .willThrow(DataIntegrityViolationException.class);  // 판매자 계정 생성 시 UNIQUE 충돌이 일어났다고 가정

        // when
        OAuth2Seller result = oAuth2SellerFacade.login(command);

        // then
        assertThat(result).isEqualTo(seller);

        // 첫번째 조회 -> 판매자 계정 없음 -> 판매자 계정 생성 -> UNIQUE 충돌 -> 판매자 재조회
        InOrder inOrder = inOrder(oAuth2SellerService);
        inOrder.verify(oAuth2SellerService).findByProviderAndProviderId(provider, providerId);
        inOrder.verify(oAuth2SellerService).createOAuth2Seller(any());
        inOrder.verify(oAuth2SellerService).findByProviderAndProviderId(provider, providerId);
    }
}