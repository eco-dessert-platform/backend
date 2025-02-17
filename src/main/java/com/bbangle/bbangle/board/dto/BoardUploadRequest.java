package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.delivery.DeliveryCompany;
import com.bbangle.bbangle.store.domain.Store;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class BoardUploadRequest {
    private Long storeId;
    private String boardTitle;
    private int price;
    private int discountRate;
    private int deliveryFee;
    private int freeShippingConditions;
    private DeliveryCompany deliveryCompany;
    private List<ProductRequest> productRequests;
    private ProductInfoNoticeRequest productInfoNotice;
    private BoardDetailRequest boardDetail;

    public Board toEntity(Store store) {
        Board board = new Board(
                store,
                boardTitle,
                price,
                discountRate,
                deliveryFee,
                freeShippingConditions,
                productInfoNotice.toEntity()
        );

        List<Product> products = productRequests.stream()
                .map(productRequest -> productRequest.toEntity(board))
                .toList();
        board.addProducts(products);

        return board;
    }
}
