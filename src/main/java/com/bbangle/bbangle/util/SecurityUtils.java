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

    public static Long getMemberIdWithAnonymous() {
        // 인증된 사용자인지 확인
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        // 만약 요청된 인증이 있다면 통과
        if (authentication != null && authentication.getPrincipal() != null) {
            // 익명의 사용자라면 null값 반환
            if (authentication instanceof AnonymousAuthenticationToken) {
                return null;
            }

            //아이디 값이 있다면 아이디값 Long으로 반환
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
