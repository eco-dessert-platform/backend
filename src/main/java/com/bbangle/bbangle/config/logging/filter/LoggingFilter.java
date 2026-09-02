package com.bbangle.bbangle.config.logging.filter;

import com.bbangle.bbangle.common.adaptor.slack.SlackAdaptor;
import com.bbangle.bbangle.common.querylistener.QueryTimerContext;
import com.bbangle.bbangle.config.logging.context.MethodExecutionTimeContext;
import com.bbangle.bbangle.config.logging.dto.RequestLoggingDTO;
import com.bbangle.bbangle.config.logging.dto.ResponseLoggingDTO;
import com.bbangle.bbangle.config.logging.util.HttpLogSupportUtil;
import com.bbangle.bbangle.config.logging.util.request.CachingRequestWrapper;
import com.bbangle.bbangle.config.logging.util.request.RequestLogSupportUtil;
import com.bbangle.bbangle.config.logging.util.request.RequestMultipartBodyReader;
import com.bbangle.bbangle.config.logging.util.response.ResponseLogSupportUtil;
import com.bbangle.bbangle.config.logging.util.response.SelectiveCachingResponseWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 로깅 필터
 * Request 로그는 doFilter 전에, Response 로그는 finally 블록에서 각각 별도로 출력한다.
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
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        MDC.put("requestId", requestId);

        try {
            // multipart는 컨트롤러 처리 후 임시 파일이 삭제될 수 있으므로,
            // chain 실행 전에 JSON 파트만 미리 읽어서 캐싱해둔다. (파일 파트는 읽지 않음)
            boolean isMultipart = HttpLogSupportUtil.isMultipart(request);
            String multipartJsonSnapshot = isMultipart ? RequestMultipartBodyReader.captureJsonParts(request) : null;
            HttpServletRequest wrappedRequest = isMultipart ? request : new CachingRequestWrapper(request);

            // Request 로그는 컨트롤러 진입 전, 요청 정보만으로 바로 출력한다.
            RequestLoggingDTO requestLog = RequestLogSupportUtil.build(wrappedRequest, requestId, isMultipart, multipartJsonSnapshot);
            if (log.isInfoEnabled()) {
                log.info(requestLog.toFullLog());
            }

            SelectiveCachingResponseWrapper wrappedResponse = new SelectiveCachingResponseWrapper(response);
            long startTime = System.currentTimeMillis();
            try {
                filterChain.doFilter(wrappedRequest, wrappedResponse);
            } finally {
                long duration = System.currentTimeMillis() - startTime;
                try {
                    ResponseLoggingDTO responseLog = ResponseLogSupportUtil.build(wrappedRequest, wrappedResponse, requestId, duration);
                    logByStatus(responseLog);

                    // API 실행 시간이 3s 이상인 경우 Slack으로 메세지 전송
                    if (duration > SLOW_REQUEST_THRESHOLD_MS) {
                        slackAdaptor.sendText("느린 요청 알림", responseLog.toSlackSummary());
                    }
                } finally {
                    wrappedResponse.copyBodyToResponse();
                }
            }
        } finally {
            QueryTimerContext.clear();
            MethodExecutionTimeContext.clear();
            MDC.remove("requestId");
        }
    }

    // API Status 코드 별 로그 레벨 처리
    private void logByStatus(ResponseLoggingDTO entry) {
        int status = entry.status();

        if (status >= 500) {
            if (log.isErrorEnabled()) {
                log.error(entry.toFullLog());
            }
        } else if (status >= 400) {
            if (log.isWarnEnabled()) {
                log.warn(entry.toFullLog());
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info(entry.toFullLog());
            }
        }
    }
}
