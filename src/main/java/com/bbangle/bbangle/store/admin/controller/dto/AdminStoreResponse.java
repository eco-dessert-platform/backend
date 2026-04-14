package com.bbangle.bbangle.store.admin.controller.dto;

import com.bbangle.bbangle.store.admin.service.model.UpdateStoreNamesInfo.UpdateStoreNames;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

public class AdminStoreResponse {

    @Builder
    @Schema(description = "판매자 스토어명 변경 요청 목록 DTO")
    public record UpdateStoreNameRequest(
        List<UpdateStoreNames> updateStoreNames,
        @Schema(description = "전체 데이터 갯수", example = "1") long totalElements,
        @Schema(description = "전체 페이지", example = "1") int totalPages,
        @Schema(description = "이전 페이지 여부", example = "false") boolean hasPrevious,
        @Schema(description = "다음 페이지 여부", example = "false") boolean hasNext
    ) {}

    @Builder
    @Schema(description = "판매자 스토어명 변경 신청 결과 DTO")
    public record UpdateStoreNameApprove(
        @Schema(description = "스토어 Id", example = "1") long storeId,
        @Schema(description = "이전 스토어 이름", example = "1") String prevName,
        @Schema(description = "변경된 스토어 이름", example = "1") String updateName,
        @Schema(description = "수정일", example = "2026-01-01T01:23:45") LocalDateTime modifiedAt
    ) {}
}
