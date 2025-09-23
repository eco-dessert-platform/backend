package com.bbangle.bbangle.seller.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.seller.controller.swagger.SellerApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SellerController implements SellerApi {

    private final ResponseService responseService;


    @Override
    public CommonResult accountVerification(String accountNumber, String sellerName) {
        return responseService.getSuccessResult();
    }
}
