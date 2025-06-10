package com.bbangle.bbangle.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "공지사항 등록 요청 DTO")
public record NotificationUploadRequest(
        @NotBlank
        @Schema(description = "공지사항 제목", example = "이벤트 안내: 여름 디저트 할인 시작!")
        String title,
        @NotBlank
        @Schema(description = "공지사항 본문", example = "시원한 여름 디저트를 20% 할인된 가격으로 만나보세요!")
        String content
) {
}
