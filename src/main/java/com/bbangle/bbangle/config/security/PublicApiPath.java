package com.bbangle.bbangle.config.security;

public class PublicApiPath {

    public static final String OAUTH_PREFIX = "/api/v1/oauth/authorization";
    public static final String AUTH_PREFIX = "/api/v1/auth";

    public static final String[] ANY_METHOD = {
        "/api/v1/token",
        AdminApiPath.PREFIX + "/login",
        AdminApiPath.PREFIX + "/logout",
        AdminApiPath.PREFIX + "/reissue",
        "/api/v1/oauth/**",
        "/api/v1/search/**",
        "/api/v1/landingpage",
        "/api/v1/store/**",
        "/api/v1/stores/**",
        "/api/v1/health/**",
        "/api/v1/push/**",
        SellerApiPath.PREFIX + "/oauth/tokens",
        PublicApiPath.AUTH_PREFIX + "/**"
    };

    public static final String[] GET_ONLY = {
        "/api/v1/boards/**",
        "/api/v1/notification/**",
        "/api/v1/boards/notification/**",
        "/api/v1/review/**",
        "/api/v1/analytics/**"
    };

    public static final String[] PATCH_OLLY = {
        "/api/v1/boards/**"
    };

}
