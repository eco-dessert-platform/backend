package com.bbangle.bbangle.config.logging;

import lombok.Builder;

/**
 * 로그 출력 담당 DTO
 * @param method HTTP 메서드 종류
 * @param uri API URI
 * @param status HTTP Response Status
 * @param requestId 로깅용 Request Id
 * @param controllerInfo API 처리 담당 Controller 메서드명
 * @param totalTimeMs API 총 처리 시간
 * @param dbTimeMs DB 쿼리 총 실행 시간
 * @param layerExecutionLog 계층별 메서드 실행 시간
 * @param requestHeaders Request Header 정보 (마스킹 처리 됨)
 * @param requestBody Request Body 정보 (마스킹 처리 됨)
 * @param responseBody Response Body 정보 (마스킹 처리 됨)
 */
@Builder
public record LoggingDTO(
    String method,
    String uri,
    int status,
    String requestId,
    String controllerInfo,
    long totalTimeMs,
    long dbTimeMs,
    String layerExecutionLog,
    String requestHeaders,
    String requestBody,
    String responseBody
) {
    private static final String SEPARATOR = "----------------------------------------------------------------";

    /**
     * 로깅용 메세지 포맷
     * @return 로깅 메세지
     */
    public String toFullLog() {
        return String.format("""
                
                %s
                - API              : [%s] (%d) %s
                %s
                - Request ID       : %s
                - Controller       : %s
                - 총 처리시간        : %d ms
                - 총 DB 처리시간     : %d ms
                - 계층별 실행시간
                %s
                %s
                - Request Header
                %s
                
                - Request Body
                %s
                %s
                - Response Body
                %s
                %s
                """,
            SEPARATOR,
            method, status, uri,
            SEPARATOR,
            requestId, controllerInfo, totalTimeMs, dbTimeMs,
            layerExecutionLog,
            SEPARATOR,
            requestHeaders,
            requestBody,
            SEPARATOR,
            responseBody,
            SEPARATOR);
    }

    /**
     * Slack 전송 용 메세지 포맷
     * @return Slack 전송 메세지
     */
    public String toSlackSummary() {
        return String.format("""
                
                - API               : [%s] (%d) %s
                - Request ID        : %s
                - Controller        : %s
                - 총 처리시간         : %d ms
                - 총 DB 처리시간      : %d ms
                - 계층별 실행시간
                %s
                """,
            method, status, uri, requestId, controllerInfo, totalTimeMs, dbTimeMs, layerExecutionLog);
    }
}
