package com.bbangle.bbangle.config.logging.util.request;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

/**
 * Request 헤더/쿠키 마스킹 전담 유틸
 */
@UtilityClass
public class RequestHeaderMaskingUtil {

    private static final Set<String> MASKED_HEADERS = Set.of("authorization");
    private static final String MASKED_VALUE = "***MASKED***";

    // Request Header 추출
    public String extractHeaders(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = request.getHeader(name);

            if (MASKED_HEADERS.contains(name.toLowerCase())) {
                value = maskToken(value);
            } else if (name.equalsIgnoreCase("cookie")) {
                value = maskCookieHeader(value);
            }
            sb.append("    ").append(name).append(": ").append(value).append("\n");
        }

        return sb.isEmpty() ? "    null" : sb.toString().stripTrailing();
    }

    // JWT 마스킹
    public String maskToken(String value) {
        if (value == null || value.isBlank()) return "null";

        String prefix = "";
        String token = value;

        if (value.startsWith("Bearer ")) {
            prefix = "Bearer ";
            token = value.substring(7);
        }

        if (token.length() <= 10) {
            return prefix + "*".repeat(token.length());
        }

        return prefix + token.substring(0, 2) + MASKED_VALUE + token.substring(token.length() - 2);
    }

    /**
     * "name1=value1; name2=value2" 형태의 Cookie 헤더에서
     * 각 쿠키의 값만 마스킹하고 이름은 그대로 남긴다.
     * 출력 예시 : name1=***MASKED***; name2=***MASKED***;
     */
    public String maskCookieHeader(String cookieHeader) {
        if (cookieHeader == null || cookieHeader.isBlank()) return "null";

        return Arrays.stream(cookieHeader.split(";"))
            .map(String::trim)
            .map(pair -> {
                int idx = pair.indexOf('=');
                if (idx < 0) return pair;
                return pair.substring(0, idx) + "=" + MASKED_VALUE;
            })
            .collect(Collectors.joining("; "));
    }
}
