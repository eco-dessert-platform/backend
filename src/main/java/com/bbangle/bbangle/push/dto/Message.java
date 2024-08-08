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

    public static Message of(FcmRequest request) {
        return Message.builder()
                .fcmToken(request.getFcmToken())
                .notification(new Notification(request))
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Notification {
        private String title;
        private String body;

        public Notification(FcmRequest request) {
            this.title = request.getTitle();
            this.body = request.getBody();
        }
    }
}
