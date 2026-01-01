package com.bbangle.bbangle.notification.admin.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LinkDto(@Schema(description = "링크 URL", example = "https://example.com")
                      String url,
                      @Schema(description = "링크 표시 텍스트", example = "더보기")
                      String linkText) {
}
