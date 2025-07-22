package com.bbangle.bbangle.image.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.image.domain.ImageCategory;
import com.bbangle.bbangle.image.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/images")
@Tag(name = "images", description = "이미지 API")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final ResponseService responseService;

    @Operation(summary = "보드 이미지 이외의 이미지 저장")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SingleResult<String> saveImg(@RequestParam ImageCategory imageCategory,
                                        @RequestParam MultipartFile file) {
        return responseService.getSingleResult(imageService.save(imageCategory, file, -1));
    }
}
