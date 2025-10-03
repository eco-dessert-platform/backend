package com.bbangle.bbangle.seller.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.seller.dto.SellerAccountUpdateRequest_v2;
import com.bbangle.bbangle.seller.dto.SellerStoreNameUpdateRequest_v2;
import com.bbangle.bbangle.seller.dto.SellerUpdateRequest_v2;
import com.bbangle.bbangle.seller.service.SellerService_v2;
import com.bbangle.bbangle.seller.swagger.SellerApi_v2;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seller/sellers")
public class SellerController_v2 implements SellerApi_v2 {

    private final ResponseService responseService;
    private final SellerService_v2 sellerService;

    @PutMapping
    @Override
    public CommonResult updateSeller(
        @RequestBody @Validated SellerUpdateRequest_v2 request,
        @AuthenticationPrincipal Long sellerId
    ) {
        sellerService.updateSeller(request, sellerId);
        return responseService.getSuccessResult();
    }

    @PatchMapping("/store-name")
    @Override
    public CommonResult updateStoreName(
        @RequestBody @Validated SellerStoreNameUpdateRequest_v2 request,
        @AuthenticationPrincipal Long sellerId
    ) {
        sellerService.updateStoreName(request, sellerId);
        return responseService.getSuccessResult();
    }

    @PatchMapping("/account")
    @Override
    public CommonResult updateAccount(
        @RequestBody @Validated SellerAccountUpdateRequest_v2 request,
        @AuthenticationPrincipal Long sellerId
    ) {
        sellerService.updateAccount(request, sellerId);
        return responseService.getSuccessResult();
    }
}