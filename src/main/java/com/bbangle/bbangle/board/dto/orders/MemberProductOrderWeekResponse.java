package com.bbangle.bbangle.board.dto.orders;

import com.bbangle.bbangle.board.domain.OrderTypeEnum;
import com.bbangle.bbangle.board.dto.OrderAvailableWeek;
import com.bbangle.bbangle.board.dto.OrderWeekByUserDTO;
import com.bbangle.bbangle.board.dto.orders.abstracts.ProductOrderResponseBase;
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
            return product.getPushType().equals(PushType.RESTOCK);
        }

        return product.getPushType().equals(PushType.DATE);
    }
}
