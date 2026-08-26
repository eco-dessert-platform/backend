package com.bbangle.bbangle.config.security.handler;

import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * 권한이 없는 요청(403)에 대해 프로젝트 공통 응답 구조로 응답한다.
 */
@Slf4j
@RequiredArgsConstructor
public class BbangleAccessDeniedHandler implements AccessDeniedHandler {

    private final ResponseService responseService;
    private final ObjectMapper objectMapper;

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) throws IOException {
        BbangleErrorCode errorCode = BbangleErrorCode.FORBIDDEN;
        log.warn("권한이 없는 요청: {} {}", request.getMethod(), request.getRequestURI());

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), responseService.getError(errorCode));
    }

}
