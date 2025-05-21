package com.bbangle.bbangle.common.service;

import com.bbangle.bbangle.common.adaptor.slack.SlackAdaptor;
import com.bbangle.bbangle.common.querylistener.QueryTimerContext;
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

        long dbTime = QueryTimerContext.getTotalTime();
        QueryTimerContext.clear();

        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();
        String slackMessage = String.format("""
                
                - API           : %s %s
                - 총 DB 처리 시간 : %d ms
                - 총 처리시간     : %d ms
                """, requestMethod, requestURI, dbTime, duration);
        log.info(slackMessage);
        if (duration > THREE_SECONDS) {
            slackAdaptor.sendText("느린 요청 알림", slackMessage);
        }
    }

}
