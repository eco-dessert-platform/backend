package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.dto.orders.ProductDtoAtBoardDetail;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderAvailableWeek {

    private Boolean monday;
    private Boolean tuesday;
    private Boolean wednesday;
    private Boolean thursday;
    private Boolean friday;
    private Boolean saturday;
    private Boolean sunday;

    public static OrderAvailableWeek from(Product product) {
        return OrderAvailableWeek.builder()
            .monday(product.isMonday())
            .tuesday(product.isTuesday())
            .wednesday(product.isWednesday())
            .thursday(product.isThursday())
            .friday(product.isFriday())
            .saturday(product.isSaturday())
            .sunday(product.isSunday())
            .build();
    }

    public static OrderAvailableWeek from(ProductDtoAtBoardDetail product) {
        return OrderAvailableWeek.builder()
            .monday(product.getMonday())
            .tuesday(product.getTuesday())
            .wednesday(product.getWednesday())
            .thursday(product.getThursday())
            .friday(product.getFriday())
            .saturday(product.getSaturday())
            .sunday(product.getSunday())
            .build();
    }

}
