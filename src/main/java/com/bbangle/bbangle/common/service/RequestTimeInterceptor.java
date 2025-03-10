package com.bbangle.bbangle.common.service;

import com.bbangle.bbangle.common.adaptor.slack.SlackAdaptor;
import com.bbangle.bbangle.exception.BbangleException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestTimeInterceptor implements HandlerInterceptor {

    private static final Long THREE_SECONDS = 3000L;
    private final SlackAdaptor slackAdaptor;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
        Object handler, Exception ex) {

        long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();

        if (duration > THREE_SECONDS) {
            String header = String.format("요청 [%s %s]", requestMethod, requestURI);
            String content = String.format("처리 시간: %dms", duration);
            slackAdaptor.sendText(header, content);
            log.debug(header + header);
        }
    }
}
