package com.bbangle.bbangle.seller.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.seller.dto.SellerStoreNameUpdateRequest;
import com.bbangle.bbangle.seller.dto.SellerUpdateRequest;
import com.bbangle.bbangle.seller.service.SellerService;
import com.bbangle.bbangle.seller.swagger.SellerApi;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/sellers")
@Tag(name = "Sellers", description = "판매자 관리 API")
public class SellerController implements SellerApi {

    private final ResponseService responseService;
    private final SellerService sellerService;

    @PutMapping
    @Override
    public CommonResult updateSeller(
        @RequestBody @Validated SellerUpdateRequest request,
        @AuthenticationPrincipal Long sellerId
    ) {
        sellerService.updateSeller(request, sellerId);
        return responseService.getSuccessResult();
    }

    @PatchMapping("/store-name")
    @Override
    public CommonResult updateStoreName(
        @RequestBody @Validated SellerStoreNameUpdateRequest request,
        @AuthenticationPrincipal Long sellerId
    ) {
        sellerService.updateStoreName(request, sellerId);
        return responseService.getSuccessResult();
    }
}