package com.bbangle.bbangle.board.dto.orders;

import com.bbangle.bbangle.board.domain.OrderTypeEnum;
import com.bbangle.bbangle.board.dto.OrderAvailableWeek;
import com.bbangle.bbangle.board.dto.OrderWeekByUserDTO;
import com.bbangle.bbangle.board.dto.orders.abstracts.ProductOrderResponseBase;
import lombok.Getter;

@Getter
public class MemberProductOrderWeekResponse extends ProductOrderResponseBase {
    private OrderAvailableWeek orderAvailableWeek;
    private OrderWeekByUserDTO appliedOrderWeek;
    private boolean isNotified;

    public MemberProductOrderWeekResponse(ProductDtoAtBoardDetail product, OrderTypeEnum orderType) {
        super(product);

        this.orderAvailableWeek = OrderAvailableWeek.from(product);
        this.appliedOrderWeek = OrderWeekByUserDTO.from(product.getDays());
        this.orderType = orderType;
        this.isSoldout = product.getSoldout();
        this.isNotified = !product.getDays().isEmpty();
    }
}
