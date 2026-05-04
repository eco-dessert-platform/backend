package com.bbangle.bbangle.paymenthold.seller.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.paymenthold.seller.controller.dto.request.PaymentHoldFilter;
import com.bbangle.bbangle.paymenthold.seller.controller.dto.response.PaymentHoldResponse.PaymentHoldPageResponse;
import com.bbangle.bbangle.paymenthold.seller.service.SellerPaymentHoldService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(SellerApiPath.PREFIX + "/payment-hold")
public class SellerPaymentHoldController implements SellerPaymentHoldApi {

    private final ResponseService responseService;
    private final SellerPaymentHoldService sellerPaymentHoldService;

    @Override
    @GetMapping
    public SingleResult<PaymentHoldPageResponse> getPaymentHolds(
        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
        Pageable pageable,
        PaymentHoldFilter filter,
        @AuthenticationPrincipal Long sellerId
    ) {
        return responseService.getSingleResult(
            sellerPaymentHoldService.getPaymentHolds(filter.toCommand(sellerId, pageable))
        );
    }
}
