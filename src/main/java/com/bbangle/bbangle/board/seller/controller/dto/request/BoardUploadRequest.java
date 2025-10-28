package com.bbangle.bbangle.board.seller.controller.dto.request;

import com.bbangle.bbangle.board.customer.domain.constant.DeliveryCompany;
import com.bbangle.bbangle.board.customer.domain.constant.DeliveryCondition;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.BoardDetail;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.store.domain.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "게시글 업로드 요청 DTO v2")
public record BoardUploadRequest(
    @Schema(description = "게시글 제목", example = "맛있는 다이어트 도시락")
    String boardTitle,

    @Schema(description = "상품 제작 시작 시간", example = "250800")
    LocalDateTime productionStartDate,

    @Schema(description = "기본 가격", example = "12000")
    int price,

    @Schema(description = "할인율 (%)", example = "10")
    int discountRate,

    @Schema(description = "최종 상품 금액", example = "10800")
    int totalPrice,

    @Schema(description = "배송 조건", example = "PAID")
    DeliveryCondition deliveryCondition,

    @Schema(description = "배송 회사")
    DeliveryCompany deliveryCompany,

    @Schema(description = "배송비", example = "3000")
    int deliveryFee,

    @Schema(description = "무료배송 최소 금액", example = "30000")
    int freeShippingConditions,

    @Schema(description = "대표 썸네일 이미지", example = "1")
    Long thumbnailImgId,

    @Schema(description = "등록할 추가 이미지 ID 리스트", example = "[1, 2, 3]")
    List<Long> productImgIds,

    @Schema(description = "상품 요청 목록")
    List<ProductRequest> productRequests,

    @Schema(description = "게시글 상세 내용 요청")
    BoardDetailRequest boardDetailRequest,

    @Schema(description = "상품 정보 고시 요청")
    ProductInfoNoticeRequest productInfoNoticeRequest
) {

    public Board toBoard(Store store) {
        Board board = new Board(
            store,
            boardTitle,
            price,
            discountRate,
            deliveryFee,
            freeShippingConditions,
            productInfoNoticeRequest.toEntity()
        );
        toProducts(board);
        toBoardDetail(board);
        return board;
    }

    private void toProducts(Board board) {
        List<Product> products = this.productRequests
            .stream()
            .map(productRequest -> productRequest.toEntity(board))
            .toList();

        board.addProducts(products);
    }

    private void toBoardDetail(Board board) {
        BoardDetail boardDetail = boardDetailRequest.toEntity(board);
        board.addBoardDetails(boardDetail);
    }
}
