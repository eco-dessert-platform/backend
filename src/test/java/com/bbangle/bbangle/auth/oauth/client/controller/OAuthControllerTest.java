package com.bbangle.bbangle.auth.oauth.client.controller;

import static com.bbangle.bbangle.common.service.ResponseService.CommonResponse.SUCCESS;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.auth.oauth.client.dto.TokenResponse;
import com.bbangle.bbangle.auth.oauth.client.facade.OAuthFacade;
import com.bbangle.bbangle.auth.oauth.client.service.OAuthService;
import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.JsonDataEncoder;
import com.bbangle.bbangle.config.security.PublicApiPath;
import com.bbangle.bbangle.config.security.SecurityConfig;
import com.bbangle.bbangle.config.security.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.oauth.CookieFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("[컨트롤러 테스트] OAuthController")
@Import({
    TestSlackAdaptorConfig.class,
    JsonDataEncoder.class,
    TokenProvider.class,
    TestJwtPropertiesConfig.class,
    ResponseService.class,
    SecurityConfig.class
})
@WebMvcTest(controllers = OAuthController.class)
@ActiveProfiles("test")
class OAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OAuthFacade facade;

    @MockBean
    private OAuthService service;

    @SpyBean
    private ResponseService responseService;

    @Test
    @DisplayName("Refresh Token 쿠키가 존재하면 토큰을 재발급한다.")
    void success_reissueToken() throws Exception {

        // given
        String refreshToken = "validRefreshToken";

        TokenResponse tokenResponse = TokenResponse.builder()
            .refreshToken("newRefreshToken")
            .accessToken("newAccessToken")
            .build();

        given(facade.reissueToken(refreshToken)).willReturn(tokenResponse);

        // when & then
        mockMvc.perform(post(PublicApiPath.AUTH_PREFIX + "/reissue")
                .cookie(CookieFixture.defaultCookie(refreshToken)))
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
        mockMvc.perform(post(PublicApiPath.AUTH_PREFIX + "/reissue"))
            .andExpect(status().isUnauthorized());

        // 쿠키가 없을 경우 OAuthFacade는 호출조차 하지 않음
        then(facade).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("Refresh Token이 유효하지 않을 경우 401을 반환한다.")
    void failure_reissueToken_invalidRefreshToken() throws Exception {

        // given
        String refreshToken = "invalidRefreshToken";

        given(facade.reissueToken(refreshToken))
            .willThrow(new BbangleException(BbangleErrorCode._UNAUTHORIZED));

        // when & then
        mockMvc.perform(post(PublicApiPath.AUTH_PREFIX + "/reissue")
                .cookie(CookieFixture.defaultCookie(refreshToken)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃 시 항상 만료된 쿠키를 반환한다.")
    void logout() throws Exception {

        // when & then
        mockMvc.perform(delete(PublicApiPath.AUTH_PREFIX + "/logout")
                .cookie(CookieFixture.defaultCookie()))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=")))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
            .andExpect(jsonPath("$.message").value(SUCCESS.getMessage()));

        then(service).should(times(1)).deleteRefreshToken("refreshToken");
    }
}