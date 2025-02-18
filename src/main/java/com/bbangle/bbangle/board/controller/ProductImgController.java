package com.bbangle.bbangle.board.controller;

import com.bbangle.bbangle.board.service.ProductImgUploadService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
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

    private final ProductImgUploadService productImgUploadService;
    private final ResponseService responseService;

    /**
     * 단일 이미지 업로드 (Board 연결 없이)
     */
    @PostMapping(value = "/image/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResult uploadImage(
            @RequestParam("image") MultipartFile image) {
        return responseService.getSingleResult(productImgUploadService.uploadSingle(image));
    }

    /**
     * 다중 이미지 업로드 (Board 연결 없이)
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResult uploadMultipleImages(
            @RequestParam("images") List<MultipartFile> images) {
        return responseService.getSingleResult(productImgUploadService.uploadMultiple(images));
    }
}
