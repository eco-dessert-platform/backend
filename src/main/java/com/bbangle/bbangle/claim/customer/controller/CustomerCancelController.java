package com.bbangle.bbangle.claim.customer.controller;

import com.bbangle.bbangle.claim.customer.controller.dto.CustomerCancelRequest;
import com.bbangle.bbangle.claim.customer.service.CustomerCancelService;
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
public class CustomerCancelController {

    private final ResponseService responseService;
    private final CustomerCancelService customerCancelService;

    @PatchMapping("/{orderId}/cancel")
    public CommonResult requestCancel(
        @PathVariable Long orderId,
        @Valid @RequestBody CustomerCancelRequest request,
        @AuthenticationPrincipal Long customerId
    ) {
        customerCancelService.requestCancel(orderId, customerId, request);
        return responseService.getSuccessResult();
    }
}
