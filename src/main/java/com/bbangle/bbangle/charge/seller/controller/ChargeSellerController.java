package com.bbangle.bbangle.charge.seller.controller;

import com.bbangle.bbangle.charge.seller.controller.dto.response.ChargeBalanceResponse;
import com.bbangle.bbangle.charge.seller.controller.swagger.ChargeSellerApi;
import com.bbangle.bbangle.charge.seller.service.ChargeService;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(SellerApiPath.PREFIX)
@RequiredArgsConstructor
public class ChargeSellerController implements ChargeSellerApi {

    private final ChargeService chargeService;
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
}
