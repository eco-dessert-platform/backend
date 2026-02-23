package com.bbangle.bbangle.order.seller.service.model;

import com.bbangle.bbangle.common.dto.SearchFormDto.DefaultSearchCondition;
import com.bbangle.bbangle.order.domain.model.CompletedOrderSearchType;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
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
    public record ShipmentRegisterCommand(
        Long orderId,
        List<Long> orderItemIds,
        String courierName,
        String trackingNumber,
        Long sellerId
    ) {
    }

    @Builder
    public record ShipmentModifyCommand(
        Long orderId,
        List<Long> orderItemIds,
        String courierName,
        String trackingNumber,
        Long sellerId
     ) {
    }
    @Builder
    public record OrderSearchCommand(
        Long sellerId,
        OrderDeliveryStatus orderDeliveryStatus,
        CompletedOrderSearchType searchType,
        DefaultSearchCondition searchCondition,
        Pageable page
    ) {
    }
}
