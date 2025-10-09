package com.bbangle.bbangle.common.adaptor.slack;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@Slf4j
@TestConfiguration
public class TestSlackAdaptorConfig {

    @Bean
    public SlackAdaptor slackAdaptor() {
        return new SlackAdaptor() {
            @Override
            public void sendAlert(HttpServletRequest httpServletRequest, Throwable t) {
                log.info("[TEST] Slack Alert skipped: {}, {}", httpServletRequest.getRequestURI(), t.getMessage());
            }

            @Override
            public void sendText(String title, String content) {
                log.info("[TEST] Slack Message skipped: {} - {}", title, content);
            }
        };
    }

}