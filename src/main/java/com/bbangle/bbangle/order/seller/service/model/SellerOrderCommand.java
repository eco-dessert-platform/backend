package com.bbangle.bbangle.order.seller.service.model;

import com.bbangle.bbangle.common.dto.SearchFormDto.DefaultSearchCondition;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import java.util.List;
import lombok.Builder;
import org.springframework.data.domain.Pageable;

public class SellerOrderCommand {

    @Builder
    public record OrderConfirmCommand(
        Long orderId,
        List<Long> orderItemIds,
        Long sellerId
    ) {
    }

    @Builder
    public record OrderSearchCommand(
        Long sellerId,
        OrderDeliveryStatus orderDeliveryStatus,
        OrderStatus orderStatus,
        DefaultSearchCondition searchCondition,
        Pageable page
    ) {
    }
}
