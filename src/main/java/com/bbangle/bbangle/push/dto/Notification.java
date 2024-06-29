package com.bbangle.bbangle.push.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Notification {
    private String fcmToken;
    private Message message;

    public static Notification of(String fcmToken, Message message) {
        return Notification.builder()
                .fcmToken(fcmToken)
                .message(message)
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Message {
        private String title;
        private String body;

        public static Message of(String title, String body) {
            return Message.builder()
                    .title(title)
                    .body(body)
                    .build();
        }
    }
}
