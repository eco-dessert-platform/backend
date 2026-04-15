package com.bbangle.bbangle.store.admin.service.model;

import com.bbangle.bbangle.store.domain.StoreNameRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;

public class UpdateStoreNamesInfo {

    @Builder
    @Schema(description = "판매자 스토어명 변경 요청 DTO")
    public record UpdateStoreNames(
        @Schema(description = "판매자 스토어명 변경 요청 Id", example = "1") Long requestId,
        @Schema(description = "스토어 id", example = "1") Long storeId,
        @Schema(description = "현재 스토어 이름", example = "빵그리") String currentName,
        @Schema(description = "변경할 스토어 이름", example = "빵그리의 오븐") String newName,
        @Schema(description = "스토어명 변경 신청일", example = "2026-01-01T01:23:45") LocalDateTime createdAt
    ) {

        public static UpdateStoreNames from(StoreNameRequest storeNameRequest) {
            return UpdateStoreNames.builder()
                .requestId(storeNameRequest.getId())
                .storeId(storeNameRequest.getStore().getId())
                .currentName(storeNameRequest.getCurrentName())
                .newName(storeNameRequest.getNewName())
                .createdAt(storeNameRequest.getCreatedAt())
                .build();
        }
    }
}
