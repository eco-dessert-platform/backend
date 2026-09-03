package com.bbangle.bbangle.config.logging.util.request;

import com.bbangle.bbangle.config.logging.dto.RequestLoggingDTO;
import com.bbangle.bbangle.config.logging.util.JsonPrettyPrinterUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import lombok.experimental.UtilityClass;

/**
 * Request 로그 DTO 조립 담당 유틸
 */
@UtilityClass
public class RequestLogSupportUtil {

    public RequestLoggingDTO build(
        HttpServletRequest request,
        String requestId,
        boolean isMultipart,
        String multipartJsonSnapshot
    ) {
        return RequestLoggingDTO.builder()
            .method(request.getMethod())
            .uri(request.getRequestURI())
            .requestId(requestId)
            .headers(RequestHeaderMaskingUtil.extractHeaders(request))
            .body(extractBody(request, isMultipart, multipartJsonSnapshot))
            .build();
    }

    private String extractBody(HttpServletRequest request, boolean isMultipart, String multipartJsonSnapshot) {
        if (isMultipart) {
            return multipartJsonSnapshot != null ? multipartJsonSnapshot : "    null";
        }

        if (!(request instanceof CachingRequestWrapper cached)) {
            return "    null";
        }

        byte[] content = cached.getCachedBody();
        if (content.length == 0) {
            return "    null";
        }

        return JsonPrettyPrinterUtil.prettyPrint(new String(content, StandardCharsets.UTF_8));
    }
}
