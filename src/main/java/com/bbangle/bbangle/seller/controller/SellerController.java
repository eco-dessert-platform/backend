package com.bbangle.bbangle.seller.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.seller.dto.SellerRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sellers")
@Tag(name = "Sellers", description = "판매자 관리 API")
public class SellerController {

    private final ResponseService responseService;

    @PostMapping
    @Operation(
        summary = "판매자 정보 수정",
        description = "기존 판매자 정보를 수정합니다."
    )
    public CommonResult createSeller(@RequestBody SellerRequest.sellerUpdateRequest request) {

        // TODO : 개발 필요
        return responseService.getSuccessResult();
    }


}