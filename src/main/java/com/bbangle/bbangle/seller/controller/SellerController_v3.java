package com.bbangle.bbangle.seller.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.seller.controller.dto.SellerRequest_v3.SellerCreateRequest;
import com.bbangle.bbangle.seller.controller.swagger.SellerApi_v3;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seller")
public class SellerController_v3 implements SellerApi_v3 {

    private final ResponseService responseService;


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
