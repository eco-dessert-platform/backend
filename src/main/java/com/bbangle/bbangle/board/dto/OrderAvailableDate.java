package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.domain.Product;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderAvailableDate {

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public static OrderAvailableDate from(Product product) {
        return OrderAvailableDate.builder()
            .startDate(product.getOrderStartDate())
            .endDate(product.getOrderEndDate())
            .build();
    }
}
