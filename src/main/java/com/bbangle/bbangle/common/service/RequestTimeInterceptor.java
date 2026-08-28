package com.bbangle.bbangle.common.service;

import com.bbangle.bbangle.common.adaptor.slack.SlackAdaptor;
import com.bbangle.bbangle.common.querylistener.QueryTimerContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

// TODO : 제거하기
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

        String serialId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        MDC.put("serialId", serialId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {

        try {
            long startTime = (Long) request.getAttribute("startTime");
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            long dbTime = QueryTimerContext.getTotalTime();

            String serialId = MDC.get("serialId");
            String requestURI = request.getRequestURI();
            String requestMethod = request.getMethod();
            String handlerInfo = extractHandlerInfo(handler);

            String slackMessage = String.format("""
            
            - REQUEST ID     : %s
            - API            : %s %s
            - TARGET         : %s
            - 총 DB 처리 시간 : %d ms
            - 총 처리시간     : %d ms
            """, serialId, requestMethod, requestURI, handlerInfo, dbTime, duration);

            log.info(slackMessage);

            if (duration > THREE_SECONDS) {
                slackAdaptor.sendText("느린 요청 알림", slackMessage);
            }
        } finally {
            QueryTimerContext.clear();
            MDC.remove("serialId");
        }
    }

    private String extractHandlerInfo(Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            String className = handlerMethod.getBean().getClass().getSimpleName();
            String methodName = handlerMethod.getMethod().getName();
            return className + "." + methodName + "()";
        }
        return handler.getClass().getSimpleName();
    }
}