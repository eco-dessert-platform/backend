package com.bbangle.bbangle.seller.seller.controller.dto;

import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.SellerStoreDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

public class SellerResponse {

    @Schema(description = "판매자가 등록한 스토어 상세 정보 DTO")
    @Builder
    public record RegisteredStoreDetail(
        @Schema(description = "판매자 ID", example = "1") Long sellerId,
        @Schema(description = "스토어 상세 정보 (등록한 스토어가 존재할 경우)", nullable = true) SellerStoreDetail store
    ) {}
}
