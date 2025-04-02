package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.BoardDetail;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.delivery.DeliveryCompany;
import com.bbangle.bbangle.store.domain.Store;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class BoardUploadRequest {
    private String boardTitle;
    private int price;
    private int discountRate;
    private int deliveryFee;
    private int freeShippingConditions;
    private DeliveryCompany deliveryCompany;
    private List<ProductRequest> productRequests;
    private ProductInfoNoticeRequest productInfoNoticeRequest;
    private BoardDetailRequest boardDetailRequest;

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

