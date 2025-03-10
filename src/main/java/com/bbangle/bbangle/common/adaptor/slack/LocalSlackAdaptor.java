package com.bbangle.bbangle.common.adaptor.slack;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Slf4j
@Profile({"default", "test"})
@Component
public class LocalSlackAdaptor implements SlackAdaptor {

    public void sendAlert(HttpServletRequest httpServletRequest, Throwable t) {
        String title = t.getMessage();
        String message = String.format(
            "- url: %s \n - message: %s ",
            httpServletRequest.getRequestURI(),
            t.getMessage()
        );
        log.info("- title: {} \n {}", title, message);
    }

    public void sendText(String title, String content) {
        title = String.format("** 개발서버알림 **\n- url: %s \n", title);
        content = String.format("- message: %s", content);

        log.info("- title: {} \n {}", title, content);
    }
}
