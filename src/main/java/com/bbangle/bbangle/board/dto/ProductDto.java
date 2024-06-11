package com.bbangle.bbangle.board.dto;

import static com.bbangle.bbangle.board.domain.OrderTypeEnum.DATE;
import static com.bbangle.bbangle.board.domain.OrderTypeEnum.WEEK;

import com.bbangle.bbangle.board.domain.OrderTypeEnum;
import com.bbangle.bbangle.board.domain.Product;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Objects;
import java.util.Random;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductDto {

    private Long id;
    private String title;
    private Boolean glutenFreeTag;
    private Boolean highProteinTag;
    private Boolean sugarFreeTag;
    private Boolean veganTag;
    private Boolean ketogenicTag;
    private Nutrient nutrient;
    private OrderTypeEnum orderType;
    @JsonInclude(Include.NON_NULL)
    private OrderAvailableWeek orderAvailableWeek;
    @JsonInclude(Include.NON_NULL)
    private OrderAvailableDate orderAvailableDate;
    private Boolean isNotified;

    public static ProductDto from(Product product) {


        Nutrient nutrientDto = getNutrient(product);
        OrderTypeEnum orderType = getOrderType(product);
        ProductDtoBuilder builder = ProductDto.builder()
            .id(product.getId())
            .title(product.getTitle())
            .glutenFreeTag(product.isGlutenFreeTag())
            .highProteinTag(product.isHighProteinTag())
            .sugarFreeTag(product.isSugarFreeTag())
            .veganTag(product.isVeganTag())
            .ketogenicTag(product.isKetogenicTag())
            .nutrient(nutrientDto)
            .orderType(orderType)
            .isNotified(new Random().nextBoolean()); // 프론트 요청으로 임시로 생성

        if (orderType.equals(OrderTypeEnum.DATE)) {
            builder.orderAvailableDate(getOrderAvailableDate(product));
        } else {
            builder.orderAvailableWeek(getOrderAvailableWeek(product));
        }

        return builder.build();
    }

    private static OrderTypeEnum getOrderType(Product product) {
        return Objects.nonNull(product.getOrderStartDate()) ? DATE : WEEK;
    }

    private static Nutrient getNutrient(Product product) {
        return Nutrient.from(product);
    }

    private static OrderAvailableDate getOrderAvailableDate(Product product) {
        return OrderAvailableDate.from(product);
    }

    private static OrderAvailableWeek getOrderAvailableWeek(Product product) {
        return OrderAvailableWeek.from(product);
    }

}
