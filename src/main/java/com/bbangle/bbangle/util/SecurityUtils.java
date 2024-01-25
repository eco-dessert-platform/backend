package com.bbangle.bbangle.util;


import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 시큐리티 유틸 클래스
 *
 */
public class SecurityUtils {

    public static Object getMemberId() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    /*private static boolean isLogin() {
        return !ANONYMOUS_USER_PRINCIPLE.equals(SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal());
    }*/
}