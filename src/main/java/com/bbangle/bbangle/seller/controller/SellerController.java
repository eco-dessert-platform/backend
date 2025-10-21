package com.bbangle.bbangle.seller.controller;

import com.bbangle.bbangle.seller.controller.dto.SellerRequest.SellerAccountUpdateRequest;
import com.bbangle.bbangle.seller.controller.dto.SellerRequest.SellerCreateRequest;
import com.bbangle.bbangle.seller.controller.dto.SellerRequest.SellerDocumentsRegisterRequest;
import com.bbangle.bbangle.seller.controller.dto.SellerRequest.SellerStoreNameUpdateRequest;
import com.bbangle.bbangle.seller.controller.dto.SellerRequest.SellerUpdateRequest;
import com.bbangle.bbangle.seller.controller.swagger.SellerApi;
import com.bbangle.bbangle.seller.service.SellerService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seller/sellers")
public class SellerController implements SellerApi {

    private final ResponseService responseService;
    private final SellerService sellerService;

    @PostMapping(value = "/documents", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public CommonResult registerDocuments(
        @ModelAttribute SellerDocumentsRegisterRequest request,
        @AuthenticationPrincipal Long memberId
    ) {
        // TODO: 구현 필요
        return responseService.getSuccessResult();
    }


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

    @PatchMapping("/account")
    @Override
    public CommonResult updateAccount(
        @RequestBody @Validated SellerAccountUpdateRequest request,
        @AuthenticationPrincipal Long sellerId
    ) {
        sellerService.updateAccount(request, sellerId);
        return responseService.getSuccessResult();
    }


    // TODO : v3
    @Override
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public CommonResult createSeller(
        @Valid @RequestPart("request") SellerCreateRequest request,
        @RequestPart("profileImage") MultipartFile profileImage
    ) {
        return responseService.getSuccessResult();
    }


    @Override
    public CommonResult accountVerification(String accountNumber, String sellerName) {
        return responseService.getSuccessResult();
    }

}
