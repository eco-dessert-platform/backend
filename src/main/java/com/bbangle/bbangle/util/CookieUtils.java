package com.bbangle.bbangle.util;

import java.time.Duration;
import org.springframework.http.ResponseCookie;

/**
 * 쿠키 유틸 클래스
 */
public class CookieUtils {

    public static ResponseCookie createCookie(String value, Duration duration) {
        return ResponseCookie.from("refreshToken", value)
            .httpOnly(true)
            // TODO : 로컬에서 사용할 때는 주석처리
            .secure(true)
            .path("/")
            .maxAge(duration)
            .sameSite("Strict")
            .build();
    }
}
