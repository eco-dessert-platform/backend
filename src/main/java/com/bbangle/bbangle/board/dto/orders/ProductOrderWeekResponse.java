package com.bbangle.bbangle.board.dto.orders;

import com.bbangle.bbangle.board.domain.OrderTypeEnum;
import com.bbangle.bbangle.board.dto.OrderAvailableWeek;
import com.bbangle.bbangle.board.dto.orders.abstracts.ProductOrderResponseBase;
import lombok.Getter;

@Getter
public class ProductOrderWeekResponse extends ProductOrderResponseBase {

    private OrderAvailableWeek orderAvailableWeek;

    public ProductOrderWeekResponse(ProductDtoAtBoardDetail product) {
        super(product);

        this.orderAvailableWeek = OrderAvailableWeek.from(product);
        this.orderType = OrderTypeEnum.WEEK;
        this.isSoldout = product.getSoldout();

    }
}
