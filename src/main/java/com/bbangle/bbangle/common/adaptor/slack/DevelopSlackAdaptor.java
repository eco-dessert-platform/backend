package com.bbangle.bbangle.common.adaptor.slack;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Profile({"dev"})
@Component
public class DevelopSlackAdaptor implements SlackAdaptor {

    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${slack.webhook-url}")
    private String WEB_HOOK_URL;

    public void sendAlert(HttpServletRequest httpServletRequest, Throwable t) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(APPLICATION_JSON);

            SlackMessage slackMessage = SlackMessage.fromException(httpServletRequest, t);
            HttpEntity<SlackMessage> request = new HttpEntity<>(slackMessage, headers);

            restTemplate.postForEntity(WEB_HOOK_URL, request, String.class);
        } catch (Exception e) {
            log.error("슬랙 전송 실패!! ", e);
            // 추가적인 예외 처리가 필요하다면 여기에 작성
        }
    }

    public void sendText(String title, String content) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(APPLICATION_JSON);

            title = String.format("** 개발서버알림 **\n- url: %s \n", title);
            content = String.format("- message: %s", content);
            SlackMessage slackMessage = SlackMessage.fromText(title, content);

            HttpEntity<SlackMessage> request = new HttpEntity<>(slackMessage, headers);

            restTemplate.postForEntity(WEB_HOOK_URL, request, String.class);
            log.error(title + content);
        } catch (Exception e) {
            log.error("슬랙 전송 실패!! ", e);
            log.error(title + content);
        }
    }
}
