package com.bbangle.bbangle.board.dto.product;

import com.bbangle.bbangle.board.domain.OrderTypeEnum;
import com.bbangle.bbangle.board.dto.Nutrient;
import com.bbangle.bbangle.board.dto.OrderAvailableWeek;
import com.bbangle.bbangle.board.dto.OrderWeekByUserDTO;
import com.bbangle.bbangle.board.dto.ProductOrderDto;

public class ProductOrderWeekResponse extends ProductOrderResponseBase{

    public ProductOrderWeekResponse(ProductOrderDto product) {
        super(product);
    }

    public static ProductOrderResponseBase create(ProductOrderDto product) {
        return new ProductOrderWeekResponse(product);
    }

    @Override
    protected ProductOrderResponse getOrderType() {
        Nutrient nutrientDto = Nutrient.from(product);

        return ProductOrderResponse.builder()
            .id(product.getId())
            .title(product.getTitle())
            .price(product.getPrice())
            .glutenFreeTag(product.getGlutenFreeTag())
            .highProteinTag(product.getHighProteinTag())
            .sugarFreeTag(product.getSugarFreeTag())
            .veganTag(product.getVeganTag())
            .ketogenicTag(product.getKetogenicTag())
            .nutrient(nutrientDto)
            .orderType(OrderTypeEnum.WEEK)
            .isSoldout(product.getSoldout())
            .orderAvailableWeek(OrderAvailableWeek.from(product))
            .appliedOrderWeek(OrderWeekByUserDTO.from(product.getDays()))
            .isNotified(isNotificated())
            .build();
    }

    private boolean isNotificated() {
        return !product.getDays().isEmpty();
    }
}
