package com.bbangle.bbangle.seller.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.seller.controller.dto.SellerRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Seller", description = "판매자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sellers")
public class SellerController {
    private final ResponseService responseService;

    @Operation(summary = "판매자 서류 등록")
    @PostMapping(value = "/documents", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public CommonResult registerDocuments(
        @ModelAttribute SellerRequest.SellerDocumentRequest request
    ) {
        // TODO: 구현 필요
        return responseService.getSuccessResult();
    }

}
