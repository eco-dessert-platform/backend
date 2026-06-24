package com.bbangle.bbangle.claim.customer.controller;

import com.bbangle.bbangle.claim.customer.controller.dto.CustomerExchangeRequest;
import com.bbangle.bbangle.claim.customer.service.CustomerExchangeService;
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
public class CustomerExchangeController {

    private final ResponseService responseService;
    private final CustomerExchangeService customerExchangeService;

    @PatchMapping("/{orderId}/exchange")
    public CommonResult requestExchange(
        @PathVariable Long orderId,
        @Valid @RequestBody CustomerExchangeRequest request,
        @AuthenticationPrincipal Long customerId
    ) {
        customerExchangeService.requestExchange(orderId, customerId, request);
        return responseService.getSuccessResult();
    }
}
