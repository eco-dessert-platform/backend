package com.bbangle.bbangle.seller.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.seller.controller.dto.SellerRequest_v1.SellerDocumentsRegisterRequest;
import com.bbangle.bbangle.seller.controller.swagger.SellerApi_v1;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seller/sellers")
public class SellerController_v1 implements SellerApi_v1 {

    private final ResponseService responseService;

    @PostMapping(value = "/documents", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public CommonResult registerDocuments(
        @ModelAttribute SellerDocumentsRegisterRequest request,
        @AuthenticationPrincipal Long memberId
    ) {
        // TODO: 구현 필요
        return responseService.getSuccessResult();
    }

}