package com.bbangle.bbangle.claim.seller.service.model;

import java.util.List;
import lombok.Builder;

@Builder
public record ReturnCreateCommand(
    Long orderId,
    List<Long> orderItemIds,
    String reason,
    Long sellerId
) {
}
