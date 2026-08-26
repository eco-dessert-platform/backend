package com.bbangle.bbangle.config.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.common.client.annotation.WithMockAuthenticationPrincipal;
import com.bbangle.bbangle.config.security.jwt.JwtProperties;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("[통합테스트] 필터단 예외 및 인증/인가 실패 응답")
class SecurityExceptionHandlingIntegrationTest {

    private static final String ADMIN_URL = AdminApiPath.PREFIX + "/notices";

    private static final String THROW_HEADER = "X-Test-Throw";
    private static final String BBANGLE_EXCEPTION = "bbangle";
    private static final String RUNTIME_EXCEPTION = "runtime";
    private static final BbangleErrorCode THROWN_ERROR_CODE = BbangleErrorCode.NOTIFICATION_NOT_FOUND;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 필터 체인 안에서 의도적으로 예외를 던지는 테스트용 필터.
     * SecurityFilterChain(order -100)보다 뒤에 등록되므로 ExceptionHandlerFilter의 하위 체인에 위치한다.
     */
    @TestConfiguration
    static class ThrowingFilterConfig {

        @Bean
        FilterRegistrationBean<Filter> throwingFilter() {
            FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(
                (request, response, chain) -> {
                    String throwType = ((HttpServletRequest) request).getHeader(THROW_HEADER);

                    if (BBANGLE_EXCEPTION.equals(throwType)) {
                        throw new BbangleException(THROWN_ERROR_CODE);
                    }
                    if (RUNTIME_EXCEPTION.equals(throwType)) {
                        throw new IllegalStateException("필터에서 발생한 예외");
                    }

                    chain.doFilter(request, response);
                }
            );
            registration.addUrlPatterns("/*");
            return registration;
        }

    }

    @Nested
    @DisplayName("ExceptionHandlerFilter")
    class ExceptionHandlerFilterTest {

        @Test
        @DisplayName("필터에서 발생한 예외도 공통 응답 구조(CommonResult)로 반환한다")
        void filterException_returnsCommonResult() throws Exception {
            // given - 서명은 유효하지만 id 클레임 타입이 잘못되어 필터에서 예외가 발생하는 토큰
            String malformedToken = malformedIdClaimToken();

            // when & then
            mockMvc.perform(get(ADMIN_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + malformedToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(-1))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @WithMockAuthenticationPrincipal(role = "ADMIN")
        @DisplayName("필터에서 BbangleException이 발생하면 해당 에러코드의 상태와 코드로 응답한다")
        void filterBbangleException_returnsMappedErrorCode() throws Exception {
            // when & then
            mockMvc.perform(get(ADMIN_URL).header(THROW_HEADER, BBANGLE_EXCEPTION))
                .andExpect(status().is(THROWN_ERROR_CODE.getHttpStatus().value()))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(THROWN_ERROR_CODE.getCode()))
                .andExpect(jsonPath("$.message").value(THROWN_ERROR_CODE.getMessage()));
        }

        @Test
        @WithMockAuthenticationPrincipal(role = "ADMIN")
        @DisplayName("필터에서 처리되지 않은 예외가 발생하면 500과 공통 응답 구조로 응답한다")
        void filterRuntimeException_returns500CommonResult() throws Exception {
            // when & then
            mockMvc.perform(get(ADMIN_URL).header(THROW_HEADER, RUNTIME_EXCEPTION))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(-1))
                .andExpect(jsonPath("$.message").value("필터에서 발생한 예외"));
        }

        private String malformedIdClaimToken() {
            Date now = new Date();
            return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + Duration.ofHours(1).toMillis()))
                .claim("id", "not-a-number")
                .claim("role", "ROLE_ADMIN")
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
                .compact();
        }

    }

    @Nested
    @DisplayName("BbangleAuthenticationEntryPoint")
    class AuthenticationEntryPointTest {

        @Test
        @DisplayName("인증되지 않은 요청은 401과 공통 응답 구조를 반환한다")
        void unauthenticated_returns401CommonResult() throws Exception {
            // when & then
            mockMvc.perform(get(ADMIN_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BbangleErrorCode._UNAUTHORIZED.getCode()))
                .andExpect(jsonPath("$.message").value(BbangleErrorCode._UNAUTHORIZED.getMessage()));
        }

    }

    @Nested
    @DisplayName("BbangleAccessDeniedHandler")
    class AccessDeniedHandlerTest {

        @Test
        @WithMockAuthenticationPrincipal(role = "CUSTOMER")
        @DisplayName("권한이 없는 요청은 403과 공통 응답 구조를 반환한다")
        void accessDenied_returns403CommonResult() throws Exception {
            // when & then
            mockMvc.perform(get(ADMIN_URL))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BbangleErrorCode.FORBIDDEN.getCode()))
                .andExpect(jsonPath("$.message").value(BbangleErrorCode.FORBIDDEN.getMessage()));
        }

    }

}
