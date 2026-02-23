package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.auth.oauth.client.OAuth2StateParser;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class OAuth2ClientValidationFilter extends OncePerRequestFilter {

    private static final String PARAM_PROFILE = "profile";

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2HandlerProperties oauth2HandlerProperties;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final OAuth2StateParser stateParser;
    private final Environment environment;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String uri = request.getRequestURI();

        // OAuth2 로그인 요청일 경우 검증
        if (uri.contains("/oauth2/authorization/")) {
            // 현재 서버의 프로파일이 Prod일 경우 profile 파라미터의 값을 prod로 고정
            // profile 파라미터의 값이 부적절할 경우 prod로 고정
            request = wrapRequestDefaultParams(request);

            // OAuth2 요청이 Kakao, Google이 아닌 경우 이전 페이지로 리다이렉트
            if (!validateClientRegistration(uri, request, response)) return;

            // user 파라미터 값이 부적절한 경우 이전 페이지로 리다이렉트
            if (!validateOAuth2Params(request, response)) return;
        }

        filterChain.doFilter(request, response);
    }

    // 파라미터 값을 덮어 씌우는 메서드
    private HttpServletRequest wrapRequestDefaultParams(HttpServletRequest request) {
        // profile 파라미터 값이 부적절한 경우 prod로 고정
        String profile = OAuth2DTO.defaultProfile(request.getParameter(PARAM_PROFILE));
        // 실행 중인 서버 프로파일 값을 추출 > profile 파라미터 값을 prod로 고정
        profile = OAuth2DTO.defaultProfile(Arrays.asList(environment.getActiveProfiles()), profile);
        String finalProfile = profile;

        return new HttpServletRequestWrapper(request) {
            @Override
            public String getParameter(String name) {
                if (PARAM_PROFILE.equals(name)) return finalProfile;
                return super.getParameter(name);
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                Map<String, String[]> map = new HashMap<>(super.getParameterMap());
                map.put(PARAM_PROFILE, new String[]{finalProfile});
                return Collections.unmodifiableMap(map);
            }

            @Override
            public String[] getParameterValues(String name) {
                if (PARAM_PROFILE.equals(name)) return new String[]{finalProfile};
                return super.getParameterValues(name);
            }

            @Override
            public Enumeration<String> getParameterNames() {
                Set<String> names = new HashSet<>(Collections.list(super.getParameterNames()));
                names.add(PARAM_PROFILE); // 이름 목록에도 반드시 포함시킴
                return Collections.enumeration(names);
            }
        };
    }

    // Kakao, Google OAuth2 요청인지 검증하는 메서드
    private boolean validateClientRegistration(
        String uri,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        String registrationId = uri.substring(uri.lastIndexOf("/") + 1);
        ClientRegistration client = clientRegistrationRepository.findByRegistrationId(registrationId);

        if (client == null) {
            BbangleErrorCode code = BbangleErrorCode.NOT_SUPPORTED_SERVER;
            log.warn("Client Registration Id: [{}] - {}", registrationId, code);
            redirectStrategy.sendRedirect(request, response, createRedirectUrl(request, code));

            return false;
        }
        return true;
    }

    // 파라미터 값 검증 메서드
    private boolean validateOAuth2Params(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        String user = request.getParameter("user");
        String profile = request.getParameter(PARAM_PROFILE);

        try {
            stateParser.getParams(user, profile);
            return true;
        } catch (OAuth2Exception e) {
            BbangleErrorCode code = e.getCode();
            log.warn("Invalid OAuth2 parameters: user={}, profile={} - {}", user, profile, code);
            redirectStrategy.sendRedirect(request, response, createRedirectUrl(request, code));
            return false;
        }
    }

    // 리다이렉트 URL 생성 메서드 - Referer 헤더를 통해 리다이렉트할 URL을 결정
    // 만약 Referer가 없을 경우 서버 자체 HTML 파일을 출력
    private String createRedirectUrl(HttpServletRequest request, BbangleErrorCode code) {
        String referer = request.getHeader("Referer");
        return oauth2HandlerProperties.getErrorUrlWithReferer(code, referer);
    }
}
