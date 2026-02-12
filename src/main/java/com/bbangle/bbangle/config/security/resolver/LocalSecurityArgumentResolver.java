package com.bbangle.bbangle.config.security.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Local 환경에서 @AuthenticationPrincipal 어노테이션이 적용된 Long 타입 파라미터에 Mock user ID를 자동으로 주입하는 ArgumentResolver
 * <p>
 * local 환경에서는 토큰 없이 모든 요청을 허용하기 때문에 SecurityContextHolder에 Authentication이 설정되지 않아
 *
 * @AuthenticationPrincipal이 null이 되는 문제를 해결함
 * <p>
 * Dev/Production 환경에서는 이 Resolver가 로드되지 않으므로 실제 JWT 토큰에서 추출한 user ID가 사용됨
 */
@Component
@Profile("local")
@Slf4j
public class LocalSecurityArgumentResolver implements HandlerMethodArgumentResolver {

    private static final Long DEFAULT_USER_ID = 1L;

    /**
     * @AuthenticationPrincipal 어노테이션이 있는 Long 타입 파라미터를 지원
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
            && parameter.getParameterType().equals(Long.class);
    }

    /**
     * Local 환경에서 기본값 userId 반환
     */
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        log.debug("[Local] Mock userId={} injected for @AuthenticationPrincipal", DEFAULT_USER_ID);
        return DEFAULT_USER_ID;
    }
}
