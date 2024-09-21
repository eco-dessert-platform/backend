package com.bbangle.bbangle.board.dto.orders;

import com.bbangle.bbangle.board.domain.OrderTypeEnum;
import com.bbangle.bbangle.board.dto.OrderAvailableWeek;
import com.bbangle.bbangle.board.dto.orders.abstracts.ProductOrderResponseBase;
import lombok.Getter;

@Getter
public class ProductOrderWeekResponse extends ProductOrderResponseBase {

    private OrderAvailableWeek orderAvailableWeek;

    public ProductOrderWeekResponse(ProductDtoAtBoardDetail product, OrderTypeEnum orderType) {
        super(product);

        this.orderAvailableWeek = OrderAvailableWeek.from(product);
        this.orderType = orderType;
        this.isSoldout = product.getSoldout();

    }
}
