package com.bbangle.bbangle.config.logging.util.response;

import com.bbangle.bbangle.common.querylistener.QueryTimerContext;
import com.bbangle.bbangle.config.logging.context.MethodExecutionTimeContext;
import com.bbangle.bbangle.config.logging.dto.ResponseLoggingDTO;
import com.bbangle.bbangle.config.logging.util.HttpLogSupportUtil;
import com.bbangle.bbangle.config.logging.util.JsonPrettyPrinterUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import lombok.experimental.UtilityClass;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Response 로그 DTO 조립 담당 유틸
 */
@UtilityClass
public class ResponseLogSupportUtil {

    public ResponseLoggingDTO build(
        HttpServletRequest request,
        SelectiveCachingResponseWrapper response,
        String requestId,
        long duration
    ) {
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);

        return ResponseLoggingDTO.builder()
            .method(request.getMethod())
            .uri(request.getRequestURI())
            .status(response.getStatus())
            .requestId(requestId)
            .controllerInfo(HttpLogSupportUtil.extractHandlerInfo(handler))
            .totalTimeMs(duration)
            .dbTimeMs(QueryTimerContext.getTotalTime())
            .layerExecutionLog(MethodExecutionTimeContext.getFormattedLog())
            .body(extractBody(response))
            .build();
    }

    private String extractBody(SelectiveCachingResponseWrapper response) {
        if (!response.isBodyCached()) {
            return "    (바이너리/스트림 응답 - 생략)";
        }

        byte[] content = response.getCachedBody();
        if (content.length == 0) {
            return "    null";
        }

        return JsonPrettyPrinterUtil.prettyPrint(new String(content, StandardCharsets.UTF_8));
    }
}
