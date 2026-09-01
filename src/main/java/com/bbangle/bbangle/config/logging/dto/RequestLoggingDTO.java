package com.bbangle.bbangle.config.logging.dto;

import lombok.Builder;

/**
 * Request 로그 출력 담당 DTO
 * @param method HTTP 메서드
 * @param uri API URI
 * @param requestId 로깅용 Request Id
 * @param headers Request Header 정보 (마스킹 처리 됨)
 * @param body Request Body 정보 (마스킹 처리 됨)
 */
@Builder
public record RequestLoggingDTO(
    String method,
    String uri,
    String requestId,
    String headers,
    String body
) {
    private static final String SEPARATOR = "----------------------------------------------------------------";

    /**
     * Request 로깅용 메세지 포맷
     */
    public String toFullLog() {
        return String.format("""
                
                %s
                [REQUEST]
                - API              : [%s] %s
                %s
                - Request ID       : %s
                - Header
                %s
                
                - Body
                %s
                %s
                """,
            SEPARATOR,
            method, uri,
            SEPARATOR,
            requestId,
            headers,
            body,
            SEPARATOR);
    }
}
