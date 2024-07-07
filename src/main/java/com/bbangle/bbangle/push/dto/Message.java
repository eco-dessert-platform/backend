package com.bbangle.bbangle.push.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Message {
    private String fcmToken;
    private Notification notification;

    public static Message of(String fcmToken, Notification notification) {
        return Message.builder()
                .fcmToken(fcmToken)
                .notification(notification)
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Notification {
        private String title;
        private String body;

        public static Notification of(FcmRequest request) {
            return Notification.builder()
                    .title(request.getTitle())
                    .body(request.getBody())
                    .build();
        }
    }
}
