package com.bbangle.bbangle.seller.admin.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "판매자 서류 ZIP 다운로드 요청 DTO")
public record AdminSellerDocumentDownloadRequest(
    @Schema(description = "서류를 다운로드할 판매자 ID 목록", example = "[1, 2, 3]")
    @NotEmpty
    @Size(max = 50, message = "한 번에 최대 50명까지 다운로드할 수 있습니다.")
    List<Long> sellerIds
) {
}
