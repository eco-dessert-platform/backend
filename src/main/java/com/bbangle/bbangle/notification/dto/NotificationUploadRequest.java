package com.bbangle.bbangle.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record NotificationUploadRequest(
    @NotBlank
    String title,
    @NotBlank
    String content
) {

}
