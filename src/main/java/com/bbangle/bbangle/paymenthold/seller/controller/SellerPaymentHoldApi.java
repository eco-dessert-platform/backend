package com.bbangle.bbangle.paymenthold.seller.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.paymenthold.seller.controller.dto.request.PaymentHoldFilter;
import com.bbangle.bbangle.paymenthold.seller.controller.dto.response.PaymentHoldResponse.PaymentHoldPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@Tag(name = "Seller Payment Hold", description = "(판매자) 지급보류 API")
public interface SellerPaymentHoldApi {

    @Operation(summary = "(판매자) 지급보류 조회")
    SingleResult<PaymentHoldPageResponse> getPaymentHolds(
        @ParameterObject Pageable pageable,
        @ParameterObject PaymentHoldFilter filter,
        @Parameter(hidden = true) Long sellerId
    );
}
