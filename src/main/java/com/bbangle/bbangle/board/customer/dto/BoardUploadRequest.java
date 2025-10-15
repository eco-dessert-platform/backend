package com.bbangle.bbangle.board.customer.dto;

import com.bbangle.bbangle.board.domain.constant.DeliveryCompany;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.BoardDetail;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@Schema(description = "게시글 업로드 요청 DTO")
public class BoardUploadRequest {

    @Schema(description = "게시글 제목", example = "맛있는 다이어트 도시락")
    private String boardTitle;

    @Schema(description = "기본 가격", example = "12900")
    private int price;

    @Schema(description = "할인율 (%)", example = "10")
    private int discountRate;

    @Schema(description = "배송비", example = "3000")
    private int deliveryFee;

    @Schema(description = "무료배송 조건 (할인 금액 제외, 순수 상품 가격 기준)", example = "30000")
    private int freeShippingConditions;

    @Schema(description = "배송 회사")
    private DeliveryCompany deliveryCompany;

    @Schema(description = "상품 요청 목록")
    private List<ProductRequest> productRequests;

    @Schema(description = "상품 정보 고시 요청")
    private ProductInfoNoticeRequest productInfoNoticeRequest;

    @Schema(description = "게시글 상세 내용 요청")
    private BoardDetailRequest boardDetailRequest;

    @Schema(description = "등록할 상품 이미지 ID 리스트", example = "[1, 2, 3]")
    private List<Long> productImgIds;

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

