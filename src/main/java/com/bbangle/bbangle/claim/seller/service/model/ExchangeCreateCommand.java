package com.bbangle.bbangle.claim.seller.service.model;

import java.util.List;
import lombok.Builder;

@Builder
public record ExchangeCreateCommand(
    Long orderId,
    List<Long> orderItemIds,
    String reason,
    String sellerComment,
    Long sellerId
) {
}
