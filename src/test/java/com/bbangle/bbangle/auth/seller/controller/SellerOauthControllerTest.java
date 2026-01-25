package com.bbangle.bbangle.auth.seller.controller;

import static com.bbangle.bbangle.common.service.ResponseService.CommonResponse.SUCCESS;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.auth.oauth.client.dto.TokenResponse;
import com.bbangle.bbangle.auth.seller.controller.dto.GenerateTokenRequest;
import com.bbangle.bbangle.auth.seller.controller.dto.GenerateTokenResponse;
import com.bbangle.bbangle.auth.seller.facade.OAuth2SellerFacade;
import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.JsonDataEncoder;
import com.bbangle.bbangle.config.security.SecurityConfig;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.config.security.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("[컨트롤러 테스트] SellerOAuth2Controller")
@Import({
    TestSlackAdaptorConfig.class,
    JsonDataEncoder.class,
    TokenProvider.class,
    TestJwtPropertiesConfig.class,
    ResponseService.class,
    SecurityConfig.class
})
@WebMvcTest(controllers = SellerOauthController.class)
@ActiveProfiles("test")
class SellerOauthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonDataEncoder jsonDataEncoder;

    @MockBean
    private OAuth2SellerFacade oAuth2SellerFacade;

    @SpyBean
    private ResponseService responseService;

    @Test
    @DisplayName("OAuth 임시 코드로 토큰을 발급한다.")
    void success_sellerToken() throws Exception {

        // given
        String code = "oAuthCode";

        GenerateTokenResponse dto = GenerateTokenResponse.of(
            "refreshToken",
            "accessToken",
            1L,
            CertificationStatus.NEW
        );

        given(oAuth2SellerFacade.generateToken(code)).willReturn(dto);

        // when & then
        mockMvc.perform(post(SellerApiPath.PREFIX + "/oauth2/tokens")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDataEncoder.encode(new GenerateTokenRequest(code)))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
            .andExpect(jsonPath("$.message").value(SUCCESS.getMessage()))
            .andExpect(jsonPath("$.result.refreshToken").value("refreshToken"))
            .andExpect(jsonPath("$.result.accessToken").value("accessToken"))
            .andExpect(jsonPath("$.result.sellerId").value(1L))
            .andExpect(jsonPath("$.result.status").value("NEW"));

        then(responseService).should(times(1)).getSingleResult(dto);
    }

    @Test
    @DisplayName("임시 코드가 없으면 400 에러를 반환한다.")
    void failure_sellerToken_400() throws Exception {
        mockMvc.perform(post(SellerApiPath.PREFIX + "/oauth2/tokens")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDataEncoder.encode(new GenerateTokenRequest("")))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("임시 코드가 유효하지 않으면 401을 반환한다.")
    void failure_sellerToken_401() throws Exception {

        // given
        String code = "invalidCode";

        given(oAuth2SellerFacade.generateToken(code))
            .willThrow(new BbangleException(BbangleErrorCode._UNAUTHORIZED));

        // when & then
        mockMvc.perform(post(SellerApiPath.PREFIX + "/oauth2/tokens")
                    .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDataEncoder.encode(new GenerateTokenRequest(code)))
            )
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Refresh Token 쿠키가 존재하면 토큰을 재발급한다.")
    void success_reissueToken() throws Exception {

        // given
        String refreshToken = "validRefreshToken";
        Cookie cookie = new Cookie("refreshToken", refreshToken);

        TokenResponse tokenResponse = TokenResponse.builder()
            .refreshToken("newRefreshToken")
            .accessToken("newAccessToken")
            .build();

        given(oAuth2SellerFacade.reissueToken(refreshToken)).willReturn(tokenResponse);

        // when & then
        mockMvc.perform(post(SellerApiPath.PREFIX + "/oauth2/reissue")
            .cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(header().string("Authorization", "Bearer newAccessToken"))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=newRefreshToken")))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
            .andExpect(jsonPath("$.message").value(SUCCESS.getMessage()));

        then(responseService).should(times(1)).getSuccessResult();
    }

    @Test
    @DisplayName("Refresh Token 쿠키가 없으면 401을 반환한다.")
    void failure_reissueToken_notExistCookie() throws Exception {

        // when & then
        mockMvc.perform(post(SellerApiPath.PREFIX + "/oauth2/reissue"))
            .andExpect(status().isUnauthorized());

        // 쿠키가 없을 경우 oAuth2SellerFacade는 호출조차 하지 않음
        then(oAuth2SellerFacade).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("Refresh Token이 유효하지 않을 경우 401을 반환한다.")
    void failure_reissueToken_invalidRefreshToken() throws Exception {

        // given
        String refreshToken = "invalidRefreshToken";
        Cookie cookie = new Cookie("refreshToken", refreshToken);

        given(oAuth2SellerFacade.reissueToken(refreshToken))
            .willThrow(new BbangleException(BbangleErrorCode._UNAUTHORIZED));

        // when & then
        mockMvc.perform(post(SellerApiPath.PREFIX + "/oauth2/reissue")
                .cookie(cookie))
            .andExpect(status().isUnauthorized());
    }
}