package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@RequiredArgsConstructor
public class OAuth2ClientValidationFilter extends OncePerRequestFilter {

    private final String PARAM_PROFILE = "profile";

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2HandlerProperties oauth2HandlerProperties;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (uri.contains("/oauth2/authorization/")) {
            request = wrapRequestDefaultParams(request);
            if (!validateClientRegistration(uri, request, response)) return;
            if (!validateOAuth2Params(request, response)) return;
        }

        filterChain.doFilter(request, response);
    }

    private HttpServletRequest wrapRequestDefaultParams(HttpServletRequest request) {
        String profile = OAuth2DTO.defaultProfile(request.getParameter(PARAM_PROFILE));

        return new HttpServletRequestWrapper(request) {
            @Override
            public String getParameter(String name) {
                if (PARAM_PROFILE.equals(name)) return profile;
                return super.getParameter(name);
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                Map<String, String[]> map = new HashMap<>(super.getParameterMap());
                map.put(PARAM_PROFILE, new String[]{profile});
                return Collections.unmodifiableMap(map);
            }

            @Override
            public String[] getParameterValues(String name) {
                if (PARAM_PROFILE.equals(name)) return new String[]{profile};
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

    private boolean validateOAuth2Params(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        String user = request.getParameter("user");
        String profile = request.getParameter(PARAM_PROFILE);

        OAuth2DTO.OAuthParams dto = OAuth2DTO.OAuthParams.builder()
            .user(user)
            .profile(profile)
            .build();

        if (!dto.valid()) {
            BbangleErrorCode code = BbangleErrorCode.INVALID_OAUTH_PARAMS;
            log.warn("Invalid OAuth2 parameters: {} - {}", dto, code);
            redirectStrategy.sendRedirect(request, response, createRedirectUrl(request, code));

            return false;
        }
        return true;
    }

    private String createRedirectUrl(HttpServletRequest request, BbangleErrorCode code) {
        // ServletUriComponentsBuilder는 현재 요청(request)의 스키마, 호스트, 포트를 자동으로 가져옵니다.
        // (Forwarded 헤더가 설정되어 있다면 그 값을 우선합니다)
        return ServletUriComponentsBuilder.fromRequestUri(request)
            .replacePath(oauth2HandlerProperties.redirect().error()) // path 교체
            .replaceQuery(null) // 기존 쿼리 제거
            .queryParam("error", code)
            .queryParam("code", code.getCode())
            .build()
            .toUriString();
    }
}
