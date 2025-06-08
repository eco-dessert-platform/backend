package com.bbangle.bbangle.token.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class TokenCookieProvider {

    private static final String ACCESS_TOKEN_COOKIE = "access_token";
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final int COOKIE_MAX_AGE = 60 * 60 * 24 * 14; // 14 days
    private static final String COOKIE_PATH = "/";
    private static final boolean COOKIE_HTTP_ONLY = true;
    private static final boolean COOKIE_SECURE = true;
    private static final String SAME_SITE = "Lax";

    @Value("${app.cookie.domain}")
    private String cookieDomain;

    public void addAccessTokenCookie(HttpServletResponse response, String accessToken) {
        ResponseCookie cookie = createCookie(ACCESS_TOKEN_COOKIE, accessToken);
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = createCookie(REFRESH_TOKEN_COOKIE, refreshToken);
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public String getAccessTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, ACCESS_TOKEN_COOKIE);
    }

    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN_COOKIE);
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
        }
        return null;
    }

    private ResponseCookie createCookie(String name, String value) {
        return ResponseCookie.from(name, value)
            .maxAge(COOKIE_MAX_AGE)
            .path(COOKIE_PATH)
            .domain(cookieDomain)
            .secure(COOKIE_SECURE)
            .httpOnly(COOKIE_HTTP_ONLY)
            .sameSite(SAME_SITE)
            .build();
    }

    public void deleteAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .maxAge(0)
                .path(COOKIE_PATH)
                .domain(cookieDomain)
                .secure(COOKIE_SECURE)
                .httpOnly(COOKIE_HTTP_ONLY)
                .sameSite(SAME_SITE)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .maxAge(0)
                .path(COOKIE_PATH)
                .domain(cookieDomain)
                .secure(COOKIE_SECURE)
                .httpOnly(COOKIE_HTTP_ONLY)
                .sameSite(SAME_SITE)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

} 
