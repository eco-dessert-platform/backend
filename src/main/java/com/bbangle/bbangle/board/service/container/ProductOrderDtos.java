package com.bbangle.bbangle.board.service.container;

import com.bbangle.bbangle.board.dto.ProductOrderDto;
import com.bbangle.bbangle.board.dto.product.ProductOrderDateResponse;
import com.bbangle.bbangle.board.dto.product.ProductOrderNonMemberResponse;
import com.bbangle.bbangle.board.dto.product.ProductOrderResponseBase;
import com.bbangle.bbangle.board.dto.product.ProductOrderRestockResponse;
import com.bbangle.bbangle.board.dto.product.ProductOrderWeekResponse;
import com.bbangle.bbangle.push.domain.Push;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;

@Getter
public class ProductOrderDtos {

    private List<ProductOrderDto> orderDtoList;

    public ProductOrderDtos(List<ProductOrderDto> orderDtoLists) {
        this.orderDtoList = orderDtoLists;
    }

    public static ProductOrderDtos of(Products products, Map<Long, Push> pushMap) {
        return new ProductOrderDtos(products.getProducts().stream()
            .map(product -> {
                Push push = pushMap.get(product.getId());
                return ProductOrderDto.of(product, push);
            })
            .toList());
    }

    public static ProductOrderDtos of(Products products) {
        return new ProductOrderDtos(products.getProducts().stream()
            .map(ProductOrderDto::of)
            .toList());
    }

    public List<ProductOrderResponseBase> convertToProductOrderNonMemberResponse() {
        return orderDtoList.stream()
            .map(ProductOrderNonMemberResponse::create)
            .toList();
    }

    public List<ProductOrderResponseBase> convertToProductOrderResponse() {
        return orderDtoList.stream()
            .map(productOrderDto -> {
                if (Objects.nonNull(productOrderDto.getOrderStartDate())) {
                    if (productOrderDto.getOrderStartDate().isBefore(LocalDateTime.now()) &&
                        productOrderDto.getOrderEndDate().isAfter(LocalDateTime.now()))
                        ProductOrderDateResponse.create(productOrderDto);

                    return ProductOrderRestockResponse.create(productOrderDto);
                }

                if (Boolean.FALSE.equals(productOrderDto.getSoldout())) {
                    return ProductOrderWeekResponse.create(productOrderDto);
                }

                return ProductOrderRestockResponse.create((productOrderDto));

            })
            .toList();
    }
}
