package com.bbangle.bbangle.config.logging.dto;

import lombok.Builder;

/**
 * Response 로그 출력 및 Slack 알림 메세지 포맷 담당 DTO
 * @param method HTTP 메서드
 * @param uri API URI
 * @param status HTTP Response Status
 * @param requestId 로깅용 Request Id
 * @param controllerInfo API 처리 담당 Controller 메서드명
 * @param totalTimeMs API 총 처리 시간
 * @param dbTimeMs DB 쿼리 총 실행 시간
 * @param layerExecutionLog 계층별 메서드 실행 시간 (prod 등 AOP 미동작 환경에서는 null → 로그에서 해당 섹션 자체 생략)
 * @param body Response Body 정보 (마스킹 처리 됨)
 */
@Builder
public record ResponseLoggingDTO(
    String method,
    String uri,
    int status,
    String requestId,
    String controllerInfo,
    long totalTimeMs,
    long dbTimeMs,
    String layerExecutionLog,
    String body
) {
    private static final String SEPARATOR = "----------------------------------------------------------------";

    /**
     * Response 로깅용 메세지 포맷
     */
    public String toFullLog() {
        StringBuilder sb = new StringBuilder();
        sb.append('\n').append(SEPARATOR).append('\n');
        sb.append("[RESPONSE]\n");
        sb.append("- API              : [%s] (%d) %s\n".formatted(method, status, uri));
        sb.append(SEPARATOR).append('\n');
        sb.append("- Request ID       : %s\n".formatted(requestId));
        sb.append("- Handler          : %s\n".formatted(controllerInfo));
        sb.append("- 총 처리시간        : %d ms\n".formatted(totalTimeMs));
        sb.append("- 총 DB 처리시간     : %d ms\n".formatted(dbTimeMs));
        appendLayerExecutionLog(sb);
        sb.append("\n- Body\n");
        sb.append(body).append('\n');
        sb.append(SEPARATOR).append('\n');
        return sb.toString();
    }

    /**
     * Slack 전송용 메세지 포맷 (실행 시간 3s 이상일 때 사용)
     */
    public String toSlackSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n- API               : [%s] (%d) %s\n".formatted(method, status, uri));
        sb.append("- Request ID        : %s\n".formatted(requestId));
        sb.append("- Handler           : %s\n".formatted(controllerInfo));
        sb.append("- 총 처리시간         : %d ms\n".formatted(totalTimeMs));
        sb.append("- 총 DB 처리시간      : %d ms\n".formatted(dbTimeMs));
        appendLayerExecutionLog(sb);
        return sb.toString();
    }

    // 계층별 실행시간은 값이 있을 때만(non-prod) 섹션을 출력한다.
    private void appendLayerExecutionLog(StringBuilder sb) {
        if (layerExecutionLog != null) {
            sb.append("- 계층별 실행시간\n");
            sb.append(layerExecutionLog).append('\n');
        }
    }
}
