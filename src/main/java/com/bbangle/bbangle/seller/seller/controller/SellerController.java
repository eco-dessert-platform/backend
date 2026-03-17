package com.bbangle.bbangle.seller.seller.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.AccountVerificationRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerAccountUpdateRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerDocumentsRegisterRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerUpdateRequest;
import com.bbangle.bbangle.seller.seller.controller.swagger.SellerApi;
import com.bbangle.bbangle.seller.seller.facade.SellerFacade;
import com.bbangle.bbangle.seller.seller.service.AccountVerificationService;
import com.bbangle.bbangle.seller.seller.service.SellerService;
import com.bbangle.bbangle.seller.seller.service.info.AccountVerificationInfo;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(SellerApiPath.PREFIX + "/sellers")
public class SellerController implements SellerApi {

    private final ResponseService responseService;
    private final SellerService sellerService;
    private final SellerFacade sellerFacade;
    private final AccountVerificationService accountVerificationService;

    @PostMapping(value = "/documents", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public CommonResult registerDocuments(
        @Valid @ModelAttribute SellerDocumentsRegisterRequest request,
        @AuthenticationPrincipal Long sellerId
    ) {
        return responseService.getListResult(sellerFacade.registerDocuments(request.toCommand(sellerId)));
    }

    // TODO : v3 - Store 상세 정보 변경 API (Store/Seller/Controller로 이동할 것)
    @PutMapping
    @Override
    public CommonResult updateSeller(
        @RequestBody @Validated SellerUpdateRequest request,
        @AuthenticationPrincipal Long sellerId
    ) {
        sellerService.updateSeller(request, sellerId);
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

    @PostMapping("/account-verifications")
    @Override
    public CommonResult accountVerification(
        @RequestBody @Valid AccountVerificationRequest request,
        @AuthenticationPrincipal Long sellerId
    ) {
        AccountVerificationInfo info = accountVerificationService.verifyAccount(request.toCommand(sellerId));
        return responseService.getSingleResult(info);
    }
}
