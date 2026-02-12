package com.bbangle.bbangle.claim.seller.controller;

import com.bbangle.bbangle.claim.seller.controller.dto.CancelDecisionRequest;
import com.bbangle.bbangle.claim.seller.controller.swagger.SellerCancelApi;
import com.bbangle.bbangle.claim.seller.service.SellerClaimService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping(SellerApiPath.PREFIX + "/cancels")
@RestController
public class SellerCancelController implements SellerCancelApi {

    private final ResponseService responseService;
    private final SellerClaimService sellerClaimService;

    @PostMapping("/{cancelId}/decision")
    public CommonResult cancelDecision(
        @PathVariable Long cancelId,
        @Valid @RequestBody CancelDecisionRequest cancelDecisionRequest,
        @AuthenticationPrincipal Long sellerId
    ) {
        sellerClaimService.decision(cancelId, sellerId,
            cancelDecisionRequest.decisionType(), cancelDecisionRequest.reason());
        return responseService.getSuccessResult();
    }
}
