package com.bbangle.bbangle.board.service.factory;

import com.bbangle.bbangle.board.dto.orders.MemberProductOrderDateResponse;
import com.bbangle.bbangle.board.dto.orders.MemberProductOrderWeekResponse;
import com.bbangle.bbangle.board.dto.orders.ProductDtoAtBoardDetail;
import com.bbangle.bbangle.board.dto.orders.ProductOrderDateResponse;
import com.bbangle.bbangle.board.dto.orders.ProductOrderWeekResponse;
import com.bbangle.bbangle.board.dto.orders.abstracts.ProductOrderResponseBase;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductOrderFactory {

    public static ProductOrderResponseBase resolve(ProductDtoAtBoardDetail product) {
        return resolve(product, null);
    }

    public static ProductOrderResponseBase resolve(ProductDtoAtBoardDetail product, Long memberId) {
        if (isValidOrderDate(product)) {
            return resolveForOrderDate(product, memberId);
        }

        return resolveForWeekOrder(product, memberId);
    }

    private static boolean isValidOrderDate(ProductDtoAtBoardDetail product) {
        return Objects.nonNull(product.getOrderStartDate()) &&
            product.getOrderStartDate().isBefore(LocalDateTime.now()) &&
            product.getOrderEndDate().isAfter(LocalDateTime.now());
    }

    private static ProductOrderResponseBase resolveForOrderDate(ProductDtoAtBoardDetail product,
        Long memberId) {
        if (isMember(memberId)) {
            return new MemberProductOrderDateResponse(product);
        }

        return new ProductOrderDateResponse(product);
    }

    private static ProductOrderResponseBase resolveForWeekOrder(ProductDtoAtBoardDetail product,
        Long memberId) {
        if (isMember(memberId)) {
            return new MemberProductOrderWeekResponse(product);
        }

        return new ProductOrderWeekResponse(product);
    }

    private static boolean isMember(Long memberId) {
        return Objects.nonNull(memberId);
    }
}
