package com.bbangle.bbangle.seller.seller.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.AccountVerificationRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerAccountUpdateRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerCreateRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerDocumentsRegisterRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerStoreNameUpdateRequest;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(SellerApiPath.PREFIX + "/sellers")
public class SellerController implements SellerApi {

    private final ResponseService responseService;
    private final SellerService sellerService;
    private final SellerFacade sellerFacade;
    private final AccountVerificationService accountVerificationService;

    /**
     * Registers seller documents uploaded via multipart/form-data.
     *
     * @param request  the multipart/form-data request containing document files and metadata
     * @param sellerId the authenticated seller's id
     * @return a list result containing information about the registered seller documents
     */
    @PostMapping(value = "/documents", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public CommonResult registerDocuments(
        @Valid @ModelAttribute SellerDocumentsRegisterRequest request,
        @AuthenticationPrincipal Long sellerId
    ) {
        return responseService.getListResult(sellerFacade.registerDocuments(request.toCommand(sellerId)));
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


    /**
     * Create a new seller using the provided creation payload and profile image.
     *
     * @param request      the seller creation payload
     * @param profileImage the seller's profile image file
     * @return             a CommonResult indicating success with no payload
     */
    @Override
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public CommonResult createSeller(
        @Valid @RequestPart("request") SellerCreateRequest request,
        @RequestPart("profileImage") MultipartFile profileImage
    ) {
        sellerFacade.registerSeller(request.toCommand(), profileImage, request.storeId());
        return responseService.getSuccessResult();
    }


    /**
     * Initiates account verification for the authenticated seller and returns verification details.
     *
     * @param request the account verification request payload
     * @param sellerId the authenticated seller's ID
     * @return a CommonResult containing AccountVerificationInfo with the verification result
     */
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