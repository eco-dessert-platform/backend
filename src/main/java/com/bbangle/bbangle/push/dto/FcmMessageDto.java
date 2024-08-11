package com.bbangle.bbangle.push.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)//test
public class FcmMessageDto {
    @JsonProperty("validate_only")
    private boolean validateOnly;
    private Message message;

    public static FcmMessageDto of(FcmRequest request) {
        return FcmMessageDto.builder()
                .validateOnly(false)
                .message(Message.builder()
                        .data(Notification.builder()
                                .title(request.getTitle())
                                .body(request.getBody())
                                .build())
                        .token(request.getFcmToken())
                        .build())
                .build();
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class Message {
        private String token;
        private Notification data;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)//test
    public static class Notification {
        private String title;
        private String body;

        public Notification(FcmRequest request) {
            this.title = request.getTitle();
            this.body = request.getBody();
        }
    }
}
