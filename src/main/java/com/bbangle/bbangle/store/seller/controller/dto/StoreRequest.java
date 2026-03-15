package com.bbangle.bbangle.store.seller.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class StoreRequest {

    public record UpdateStoreNameRequest(
        @Schema(description = "변경할 스토어명", example = "빵그리의 오븐 1호점")
        @Size(min = 1, max = 50, message = "스토어명은 1자 이상 50자 이하로 입력해주세요.")
        @NotBlank(message = "스토어명은 필수입니다.")
        String newName
    ) {}
}
