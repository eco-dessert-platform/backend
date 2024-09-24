package com.bbangle.bbangle.board.dto.orders;

import com.bbangle.bbangle.board.domain.OrderTypeEnum;
import com.bbangle.bbangle.board.dto.OrderAvailableDate;
import com.bbangle.bbangle.board.dto.orders.abstracts.ProductOrderResponseBase;
import com.bbangle.bbangle.push.domain.PushType;
import lombok.Getter;

@Getter
public class MemberProductOrderDateResponse extends ProductOrderResponseBase {

    private OrderAvailableDate orderAvailableDate;
    private boolean isNotified;

    public MemberProductOrderDateResponse(ProductDtoAtBoardDetail product) {
        super(product);

        this.orderAvailableDate = OrderAvailableDate.from(product);
        this.orderType = OrderTypeEnum.DATE;
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
