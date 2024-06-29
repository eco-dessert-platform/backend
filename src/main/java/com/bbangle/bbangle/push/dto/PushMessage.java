package com.bbangle.bbangle.push.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PushMessage {
    private boolean validateOnly;
    private Notification notification;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Notification {
        private Message message;
        private String token;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Message {
        private String title;
        private String body;
        private String image;
    }
}
