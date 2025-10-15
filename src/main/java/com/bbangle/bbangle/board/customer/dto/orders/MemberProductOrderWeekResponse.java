package com.bbangle.bbangle.board.customer.dto.orders;

import com.bbangle.bbangle.board.domain.OrderTypeEnum;
import com.bbangle.bbangle.board.customer.dto.OrderAvailableWeek;
import com.bbangle.bbangle.board.customer.dto.OrderWeekByUserDTO;
import com.bbangle.bbangle.board.customer.dto.orders.abstracts.ProductOrderResponseBase;
import com.bbangle.bbangle.push.domain.PushType;
import lombok.Getter;

@Getter
public class MemberProductOrderWeekResponse extends ProductOrderResponseBase {
    private OrderAvailableWeek orderAvailableWeek;
    private OrderWeekByUserDTO appliedOrderWeek;
    private boolean isNotified;

    public MemberProductOrderWeekResponse(ProductDtoAtBoardDetail product) {
        super(product);

        this.orderAvailableWeek = OrderAvailableWeek.from(product);
        this.appliedOrderWeek = OrderWeekByUserDTO.from(product.getDays());
        this.orderType = OrderTypeEnum.WEEK;
        this.isSoldout = product.getSoldout();
        this.isNotified = judgeIsNotified(product);
    }

    public boolean judgeIsNotified(ProductDtoAtBoardDetail product) {
        if (Boolean.TRUE.equals(product.getSoldout())) {
            return PushType.RESTOCK.equals(product.getPushType());
        }

        return PushType.WEEK.equals(product.getPushType());
    }
}
