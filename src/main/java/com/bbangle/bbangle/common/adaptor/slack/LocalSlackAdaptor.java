package com.bbangle.bbangle.common.adaptor.slack;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
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
}
