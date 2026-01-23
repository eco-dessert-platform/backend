package com.bbangle.bbangle.claim.seller.controller;

import com.bbangle.bbangle.claim.seller.controller.dto.ReturnDecisionRequest;
import com.bbangle.bbangle.claim.seller.controller.swagger.SellerReturnApi;
import com.bbangle.bbangle.claim.seller.service.SellerReturnService;
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
@RequestMapping(SellerApiPath.PREFIX + "/returns")
@RestController
public class SellerReturnController implements SellerReturnApi {

    private final ResponseService responseService;
    private final SellerReturnService sellerReturnService;

    @PostMapping("/{returnId}/decision")
    public CommonResult returnDecision(
        @PathVariable Long returnId,
        @Valid @RequestBody ReturnDecisionRequest returnDecisionRequest,
        @AuthenticationPrincipal Long sellerId
    ) {
        sellerReturnService.returnDecision(returnId, sellerId, returnDecisionRequest);
        return responseService.getSuccessResult();
    }

}
