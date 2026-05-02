package com.bbangle.bbangle.charge.seller.controller;

import com.bbangle.bbangle.charge.seller.controller.dto.request.WithdrawalRequest;
import com.bbangle.bbangle.charge.seller.controller.dto.response.ChargeBalanceResponse;
import com.bbangle.bbangle.charge.seller.controller.swagger.SellerChargeApi;
import com.bbangle.bbangle.charge.seller.service.SellerChargeService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(SellerApiPath.PREFIX)
@RequiredArgsConstructor
public class SellerChargeController implements SellerChargeApi {

    private final SellerChargeService chargeService;
    private final ResponseService responseService;

    @Override
    @GetMapping("/charge-balance")
    public SingleResult<ChargeBalanceResponse> getChargeBalance(
        @AuthenticationPrincipal Long sellerId,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        @ParameterObject
        Pageable pageable
    ) {
        ChargeBalanceResponse response = chargeService.getChargeBalance(
            sellerId,
            startDate,
            endDate,
            pageable
        );
        return responseService.getSingleResult(response);
    }

    @Override
    @PostMapping("/charge-balance/withdrawal")
    public CommonResult requestWithdrawal(
        @AuthenticationPrincipal Long sellerId,
        @RequestHeader("X-Transaction-Id") String transactionId,
        @Valid @RequestBody WithdrawalRequest request
    ) {
        chargeService.requestWithdrawal(request.toCommand(sellerId, transactionId));
        return responseService.getSuccessResult();
    }
}
