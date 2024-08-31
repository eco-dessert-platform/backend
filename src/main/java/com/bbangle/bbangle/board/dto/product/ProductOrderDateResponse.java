package com.bbangle.bbangle.board.dto.product;

import com.bbangle.bbangle.board.domain.OrderTypeEnum;
import com.bbangle.bbangle.board.dto.Nutrient;
import com.bbangle.bbangle.board.dto.OrderAvailableDate;
import com.bbangle.bbangle.board.dto.ProductOrderDto;

public class ProductOrderDateResponse extends ProductOrderResponseBase {

    public ProductOrderDateResponse(ProductOrderDto product) {
        super(product);
    }

    public static ProductOrderResponseBase create(ProductOrderDto product) {
        return new ProductOrderDateResponse(product);
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
            .orderAvailableDate(OrderAvailableDate.from(product))
            .orderType(OrderTypeEnum.DATE)
            .isSoldout(product.getSoldout())
            .isNotified(product.getIsActive())
            .build();
    }
}
