package com.bbangle.bbangle.board.admin.controller.dto;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "관리자 상품 목록 조회 항목")
public record AdminProductResponse(
    @Schema(description = "상품 ID", example = "1001")
    Long productId,

    @Schema(description = "스토어명", example = "그린베이커리")
    String storeName,

    @Schema(description = "상품명", example = "저당 통밀 식빵")
    String productName,

    @Schema(description = "상품 기본가", example = "6800")
    Integer productPrice,

    @Schema(description = "상품 옵션 목록")
    List<AdminProductOptionResponse> productOptions
) {

    @Schema(description = "관리자 상품 옵션 정보")
    public record AdminProductOptionResponse(
        @Schema(description = "옵션 ID", example = "2001")
        Long optionId,

        @Schema(description = "옵션명", example = "기본")
        String optionName,

        @Schema(description = "옵션 추가 가격", example = "0")
        Integer price,

        @Schema(description = "재고 수량", example = "120")
        Integer stock,

        @Schema(description = "상품 태그 목록", example = "[\"비건\", \"저지방\"]")
        List<String> tags
    ) {

        public static AdminProductOptionResponse fromEntity(Product product) {
            return new AdminProductOptionResponse(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                product.getStock(),
                product.getTags()
            );
        }
    }

    public static AdminProductResponse fromEntity(Board board) {
        List<AdminProductOptionResponse> productOptions = board.getProducts().stream()
            .map(AdminProductOptionResponse::fromEntity)
            .toList();

        return new AdminProductResponse(
            board.getId(),
            board.getStore().getName(),
            board.getTitle(),
            board.getPrice(),
            productOptions
        );
    }

}
