package com.bbangle.bbangle.config.logging.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import lombok.experimental.UtilityClass;
import org.springframework.web.method.HandlerMethod;

/**
 * Request/Response 로깅 양쪽에서 공통으로 쓰이는 분류·추출 유틸
 * (Content-Type 판별, Multipart 여부, 핸들러 정보 추출)
 */
@UtilityClass
public class HttpLogSupportUtil {

    private static final Set<String> LOGGABLE_CONTENT_PREFIXES = Set.of(
        "application/json", "text/", "application/xml", "application/x-www-form-urlencoded"
    );

    // MultiPart 검증
    public boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    // 로깅 가능한 ContentType 검증
    public boolean isLoggableContentType(String contentType) {
        if (contentType == null) return false;
        String lower = contentType.toLowerCase();
        return LOGGABLE_CONTENT_PREFIXES.stream().anyMatch(lower::startsWith);
    }

    // 담당 API 처리 클래스 및 메서드 정보 추출
    public String extractHandlerInfo(Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            return handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName() + "()";
        }

        if (handler == null) return "null";

        return handler.getClass().getSimpleName();
    }
}
