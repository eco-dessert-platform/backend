package com.bbangle.bbangle.board.dto;


import static com.bbangle.bbangle.board.domain.OrderTypeEnum.DATE;
import static com.bbangle.bbangle.board.domain.OrderTypeEnum.WEEK;

import com.bbangle.bbangle.board.domain.OrderTypeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductOrderResponse {

    private Long id;
    private String title;
    private Boolean glutenFreeTag;
    private Boolean highProteinTag;
    private Boolean sugarFreeTag;
    private Boolean veganTag;
    private Boolean ketogenicTag;
    private Integer price;
    private Nutrient nutrient;
    private OrderTypeEnum orderType;
    @JsonInclude(Include.NON_NULL)
    private OrderAvailableWeek orderAvailableWeek;
    @JsonInclude(Include.NON_NULL)
    private OrderWeekByUserDTO appliedOrderWeek;
    @JsonInclude(Include.NON_NULL)
    private OrderAvailableDate orderAvailableDate;
    @JsonInclude(Include.NON_NULL)
    private OrderDateByUserDTO appliedOrderDate;
    private Boolean isSoldout;

    public static ProductOrderResponse from(ProductOrderDto product) {
        Nutrient nutrientDto = getNutrient(product);
        OrderTypeEnum orderType = getOrderType(product);
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
            .orderType(orderType)
            .isSoldout(product.getSoldout());

        if (orderType.equals(OrderTypeEnum.DATE)) {
            builder.orderAvailableDate(getOrderAvailableDate(product));
            builder.appliedOrderDate(getAppliedOrderDate(product));
        } else {
            builder.orderAvailableWeek(getOrderAvailableWeek(product));
            builder.appliedOrderWeek(getAppliedOrderWeek(product));
        }

        return builder.build();
    }

    private static OrderTypeEnum getOrderType(ProductOrderDto product) {
        return Objects.nonNull(product.getOrderStartDate()) ? DATE : WEEK;
    }

    private static Nutrient getNutrient(ProductOrderDto product) {
        return Nutrient.from(product);
    }

    private static OrderWeekByUserDTO getAppliedOrderWeek(ProductOrderDto product) {
        return OrderWeekByUserDTO.from(product.getDays());
    }

    private static OrderDateByUserDTO getAppliedOrderDate(ProductOrderDto product) {
        Boolean isActive = product.getIsActive() != null ? product.getIsActive() : false;
        return OrderDateByUserDTO.from(isActive);
    }

    private static OrderAvailableDate getOrderAvailableDate(ProductOrderDto product) {
        return OrderAvailableDate.from(product);
    }

    private static OrderAvailableWeek getOrderAvailableWeek(ProductOrderDto product) {

        return OrderAvailableWeek.from(product);
    }
}
