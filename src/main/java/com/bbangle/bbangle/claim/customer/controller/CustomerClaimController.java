package com.bbangle.bbangle.claim.customer.controller;

import com.bbangle.bbangle.claim.customer.controller.dto.CustomerCancelRequest;
import com.bbangle.bbangle.claim.customer.controller.dto.CustomerExchangeRequest;
import com.bbangle.bbangle.claim.customer.controller.dto.CustomerReturnRequest;
import com.bbangle.bbangle.claim.customer.service.CustomerClaimService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.CustomerApiPath;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping(CustomerApiPath.PREFIX + "/orders")
@RestController
public class CustomerClaimController {

    private final ResponseService responseService;
    private final CustomerClaimService customerClaimService;

    @PatchMapping("/{orderId}/cancel")
    public CommonResult requestCancel(
        @PathVariable Long orderId,
        @Valid @RequestBody CustomerCancelRequest request,
        @AuthenticationPrincipal Long customerId
    ) {
        customerClaimService.cancelOrder(orderId, customerId, request);
        return responseService.getSuccessResult();
    }

    @PatchMapping("/{orderId}/return")
    public CommonResult requestReturn(
        @PathVariable Long orderId,
        @Valid @RequestBody CustomerReturnRequest request,
        @AuthenticationPrincipal Long customerId
    ) {
        customerClaimService.requestReturn(orderId, customerId, request);
        return responseService.getSuccessResult();
    }

    @PatchMapping("/{orderId}/exchange")
    public CommonResult requestExchange(
        @PathVariable Long orderId,
        @Valid @RequestBody CustomerExchangeRequest request,
        @AuthenticationPrincipal Long customerId
    ) {
        customerClaimService.requestExchange(orderId, customerId, request);
        return responseService.getSuccessResult();
    }
}
