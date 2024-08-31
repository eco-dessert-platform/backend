package com.bbangle.bbangle.board.dto.product;

import com.bbangle.bbangle.board.dto.Nutrient;
import com.bbangle.bbangle.board.dto.OrderAvailableDate;
import com.bbangle.bbangle.board.dto.OrderAvailableWeek;
import com.bbangle.bbangle.board.dto.ProductOrderDto;
import com.bbangle.bbangle.board.dto.product.ProductOrderResponse.ProductOrderResponseBuilder;
import java.util.Objects;

public class ProductOrderNonMemberResponse extends ProductOrderResponseBase {

    public ProductOrderNonMemberResponse(ProductOrderDto product) {
        super(product);
    }

    public static ProductOrderResponseBase create(ProductOrderDto product) {
        return new ProductOrderNonMemberResponse(product);
    }

    @Override
    protected ProductOrderResponse getOrderType() {
        Nutrient nutrientDto = Nutrient.from(product);

        ProductOrderResponseBuilder builder = ProductOrderResponse.builder()
            .id(product.getId())
            .title(product.getTitle())
            .price(product.getPrice())
            .glutenFreeTag(product.getGlutenFreeTag())
            .highProteinTag(product.getHighProteinTag())
            .sugarFreeTag(product.getSugarFreeTag())
            .veganTag(product.getVeganTag())
            .ketogenicTag(product.getKetogenicTag())
            .nutrient(nutrientDto)
            .isSoldout(product.getSoldout());

        if (Objects.nonNull(product.getOrderStartDate())) {
            builder.orderAvailableDate(OrderAvailableDate.from(product));
        } else {
            builder.orderAvailableWeek(OrderAvailableWeek.from((product)));
        }

        return builder.build();
    }
}
