package com.bbangle.bbangle.config.security;

public class CustomerApiPath {

    public static final String PREFIX = "/api/v1/customer";

    public static final String[] ANY_METHOD = {
        "/api/v1/boards/folders/**",
        PREFIX + "/orders/**"
    };

}
