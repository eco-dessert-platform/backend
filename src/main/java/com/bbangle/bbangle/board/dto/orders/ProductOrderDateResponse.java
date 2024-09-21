package com.bbangle.bbangle.board.dto.orders;

import com.bbangle.bbangle.board.domain.OrderTypeEnum;
import com.bbangle.bbangle.board.dto.OrderAvailableDate;
import com.bbangle.bbangle.board.dto.orders.abstracts.ProductOrderResponseBase;
import lombok.Getter;

@Getter
public class ProductOrderDateResponse extends ProductOrderResponseBase {

    private OrderAvailableDate orderAvailableDate;

    public ProductOrderDateResponse(ProductDtoAtBoardDetail product, OrderTypeEnum orderType) {
        super(product);

        this.orderAvailableDate = OrderAvailableDate.from(product);
        this.orderType = orderType;
        this.isSoldout = product.getSoldout();
    }
}
