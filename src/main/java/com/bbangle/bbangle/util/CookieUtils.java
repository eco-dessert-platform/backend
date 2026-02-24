package com.bbangle.bbangle.util;

import java.time.Duration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseCookie;

/**
 * 쿠키 유틸 클래스
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieUtils {

    public static ResponseCookie createCookie(String value, Duration duration) {
        return ResponseCookie.from("refreshToken", value)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(duration)
            .sameSite("Strict")
            .build();
    }
}
