package com.bbangle.bbangle.common.adaptor.slack;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Profile({"production"})
@Component
@RequiredArgsConstructor
public class RealSlackAdaptor implements SlackAdaptor {

    private static final String WEB_HOOK_URL = "https://hooks.slack.com/services/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";
    private final RestTemplate restTemplate = new RestTemplate();

    public void sendAlert(HttpServletRequest httpServletRequest, Throwable t) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);

        SlackMessage slackMessage = buildMessage(httpServletRequest, t);
        HttpEntity<SlackMessage> request = new HttpEntity<>(slackMessage, headers);

        try {
            restTemplate.postForEntity(WEB_HOOK_URL, request, String.class);
        } catch (Exception e) {
            log.error("슬랙 전송 실패!! ", e);
        }
    }

    private SlackMessage buildMessage(HttpServletRequest request, Throwable throwable) {
        String title = throwable.getMessage();
        String message = String.format(
            "- url: %s \n - 위치: %s \n - message: %s ",
            request.getRequestURI(),
            extractMethodPosition(throwable),
            throwable.getMessage()
        );

        return new SlackMessage(
            List.of(
                createMessageTitle(title),
                createMessageBody(message)
            )
        );
    }

    private String extractMethodPosition(Throwable t) {
        Optional<StackTraceElement> optional = Arrays.stream(t.getStackTrace())
            .filter(it -> it.getClassName().contains("bbangle"))
            .findFirst();

        StackTraceElement targetElement = optional.orElseGet(() -> t.getStackTrace()[0]);
        return String.format("%s, %s", targetElement.getClassName(), targetElement.getMethodName());
    }

    private SlackBlock createMessageTitle(String title) {
        return new SlackBlock(
            "header",
            new SlackText(title)
        );
    }

    private SlackBlock createMessageBody(String message) {
        return new SlackBlock(
            "section",
            new SlackText(message)
        );
    }

    public record SlackMessage(List<SlackBlock> blocks) {

    }

    public record SlackBlock(String type, SlackText text) {

    }

    @Data
    public static class SlackText {

        private final String type = "plain_text";
        private final String text;
    }
}
