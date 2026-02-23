package com.bbangle.bbangle.fixture.oauth;

import jakarta.servlet.http.Cookie;

public final class CookieFixture {

    private CookieFixture() {}

    public static Cookie defaultCookie() {
        return new Cookie("refreshToken", "refreshToken");
    }

    public static Cookie defaultCookie(String value) {
        return new Cookie("refreshToken", value);
    }
}
