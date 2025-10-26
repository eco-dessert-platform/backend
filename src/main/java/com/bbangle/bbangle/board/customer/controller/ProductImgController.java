package com.bbangle.bbangle.board.customer.controller;

import com.bbangle.bbangle.board.customer.dto.ProductImgResponse;
import com.bbangle.bbangle.board.customer.service.ProductImgService;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/product-images")
@RequiredArgsConstructor
public class ProductImgController {

    private final ProductImgService productImgService;
    private final ResponseService responseService;

    @PostMapping(value = "/image/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Board 연결 없이 상품 이미지 1 개 업로드")
    public SingleResult<ProductImgResponse> uploadImage(
        @RequestParam("image") MultipartFile image) {
        return responseService.getSingleResult(productImgService.uploadSingle(image));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Board 연결 없이 상품 이미지 여러 개 업로드")
    public SingleResult<List<ProductImgResponse>> uploadMultipleImages(
        @RequestParam("images") List<MultipartFile> images) {
        return responseService.getSingleResult(productImgService.uploadMultiple(images));
    }
}
