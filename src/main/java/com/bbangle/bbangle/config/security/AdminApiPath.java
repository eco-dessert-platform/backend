package com.bbangle.bbangle.config.security;

public class AdminApiPath {

    public static final String PREFIX = "/api/v1/admin";

    public static String[] ANY_METHOD = {
        PREFIX + "/**"
    };

}
