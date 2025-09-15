package com.bbangle.bbangle.seller.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.seller.controller.swagger.SellerApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SellerController implements SellerApi {

    private final ResponseService responseService;

    @Override
    public CommonResult createSeller(@RequestBody SellerRequest.sellerCreateRequest request) {

        // TODO : 비즈니스 로직 수행했다는 가정

        return responseService.getSuccessResult();
    }


}
