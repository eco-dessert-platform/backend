package com.bbangle.bbangle.util;


import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 시큐리티 유틸 클래스
 */
public class SecurityUtils {

    public static Long getMemberId() {
        return Long.valueOf(String.valueOf(SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal()));
    }

    public static Long getUserIdWithAnonymous() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication != null && authentication.getPrincipal() != null) {
            if (authentication instanceof AnonymousAuthenticationToken) {
                // 익명 사용자의 처리 방식을 여기에 추가
                return null; // 또는 특별한 값을 반환하거나 예외를 던질 수 있습니다.
            }

            return Long.valueOf(String.valueOf(authentication.getPrincipal()));
        }

        return null;
    }



    /*private static boolean isLogin() {
        return !ANONYMOUS_USER_PRINCIPLE.equals(SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal());
    }*/
}
