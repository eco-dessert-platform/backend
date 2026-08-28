package com.bbangle.bbangle.config.logging;

import com.bbangle.bbangle.common.adaptor.slack.SlackAdaptor;
import com.bbangle.bbangle.common.querylistener.QueryTimerContext;
import com.bbangle.bbangle.util.HttpRequestLogSupportUtil;
import com.bbangle.bbangle.util.JsonPrettyPrinterUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * 로깅 필터
 */
@Slf4j
@RequiredArgsConstructor
public class LoggingFilter extends OncePerRequestFilter {

    private static final long SLOW_REQUEST_THRESHOLD_MS = 3000L;

    // 로깅 제외 대상 (Swagger, 헬스체크 등 API 요청이 아닌 것들)
    private static final List<String> EXCLUDED_PATH_PATTERNS = List.of(
        // Swagger
        "/",
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/webjars/**",

        // Monitoring / Health Check
        "/actuator/**",

        // 기타
        "/favicon.ico",
        "/error"
    );

    private final SlackAdaptor slackAdaptor;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 특정 URL에서는 로깅 필터가 동작하지 않도록 설정
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return EXCLUDED_PATH_PATTERNS.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        MDC.put("requestId", requestId);

        boolean isMultipart = HttpRequestLogSupportUtil.isMultipart(request);

        // multipart는 컨트롤러 처리 후 임시 파일이 삭제될 수 있으므로,
        // chain 실행 전에 텍스트 파트만 미리 읽어서 캐싱해둔다.
        String multipartBodySnapshot = isMultipart ? MultipartBodyReader.captureTextParts(request) : null;

        HttpServletRequest wrappedRequest = isMultipart ? request : new CachedBodyHttpServletRequest(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            try {
                LoggingDTO logDTO = buildLoggingDTO(wrappedRequest, wrappedResponse, requestId, duration, isMultipart, multipartBodySnapshot);
                logByStatus(logDTO);

                // API 실행 시간이 3s 이상인 경우 Slack으로 메세지 전송
                if (duration > SLOW_REQUEST_THRESHOLD_MS) {
                    slackAdaptor.sendText("느린 요청 알림", logDTO.toSlackSummary());
                }
            } finally {
                wrappedResponse.copyBodyToResponse();
                QueryTimerContext.clear();
                MethodExecutionTimeContext.clear();
                MDC.remove("requestId");
            }
        }
    }

    // API Status 코드 별 로그 레벨 처리
    private void logByStatus(LoggingDTO entry) {
        int status = entry.status();
        String message = entry.toFullLog();

        if (status >= 500) {
            log.error(message);
        } else if (status >= 400) {
            log.warn(message);
        } else {
            log.info(message);
        }
    }

    // 로깅용 DTO 생성 헬퍼 메서드
    private LoggingDTO buildLoggingDTO(
        HttpServletRequest request,
        ContentCachingResponseWrapper response,
        String requestId,
        long duration,
        boolean isMultipart,
        String multipartBodySnapshot
    ) {
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        String controllerInfo = HttpRequestLogSupportUtil.extractHandlerInfo(handler);
        String extractHeader = HttpRequestLogSupportUtil.extractHeaders(request);

        return LoggingDTO.builder()
            .method(request.getMethod())
            .uri(request.getRequestURI())
            .status(response.getStatus())
            .requestId(requestId)
            .controllerInfo(controllerInfo)
            .totalTimeMs(duration)
            .dbTimeMs(QueryTimerContext.getTotalTime())
            .layerExecutionLog(MethodExecutionTimeContext.getFormattedLog())
            .requestHeaders(extractHeader)
            .requestBody(extractRequestBody(request, isMultipart, multipartBodySnapshot))
            .responseBody(extractResponseBody(response))
            .build();
    }

    // RequestBody 추출
    private String extractRequestBody(HttpServletRequest request, boolean isMultipart, String multipartBodySnapshot) {
        if (isMultipart) {
            return multipartBodySnapshot != null ? multipartBodySnapshot : "    null";
        }

        if (!(request instanceof CachedBodyHttpServletRequest cached)) {
            return "    null";
        }

        byte[] content = cached.getCachedBody();
        if (content.length == 0) {
            return "    null";
        }

        return JsonPrettyPrinterUtil.prettyPrint(new String(content, StandardCharsets.UTF_8));
    }

    // Response Body 추출
    private String extractResponseBody(ContentCachingResponseWrapper response) {
        String contentType = response.getContentType();
        if (contentType != null && !HttpRequestLogSupportUtil.isLoggableContentType(contentType)) {
            return "    (바이너리/스트림 응답 - 생략)";
        }

        // Response의 스트림 정보 추출
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return "    null";
        }

        return JsonPrettyPrinterUtil.prettyPrint(new String(content, StandardCharsets.UTF_8));
    }
}
