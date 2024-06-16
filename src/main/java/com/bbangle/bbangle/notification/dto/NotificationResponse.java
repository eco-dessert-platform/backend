package com.bbangle.bbangle.notification.dto;

public record NotificationResponse(
    Long id,
    String title,
    String content,
    String createdAt
) {

    public NotificationResponse(Long id, String title, String content, String createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }
}
