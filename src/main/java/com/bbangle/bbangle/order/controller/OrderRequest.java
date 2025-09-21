package com.bbangle.bbangle.order.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class OrderRequest {

    public record OrderSearchRequest(
        @NotNull
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
        String startDate,
        @NotNull
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
        String endDate,
        String deliveryStatus,
        String fieldType,
        String keyword,
        @NotNull
        int page,
        @NotNull
        int size
    ) {

    }
}
