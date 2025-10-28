package com.bbangle.bbangle.board.customer.dto.orders;

import com.bbangle.bbangle.board.customer.dto.OrderAvailableWeek;
import com.bbangle.bbangle.board.customer.dto.orders.abstracts.ProductOrderResponseBase;
import com.bbangle.bbangle.board.domain.OrderTypeEnum;
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
