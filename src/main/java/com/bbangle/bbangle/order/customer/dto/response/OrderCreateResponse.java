package com.bbangle.bbangle.order.customer.dto.response;

import java.util.List;

public record OrderCreateResponse(
    List<Long> orderIds,
    String representativeOrderNumber
) {}
