package com.bbangle.bbangle.review.customer.dto;

import com.bbangle.bbangle.image.domain.ImageCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "리뷰 이미지 업로드 요청 DTO")
public record ReviewImageUploadRequest(
    @Schema(description = "업로드할 이미지 파일 목록", type = "string", format = "binary")
    List<MultipartFile> images,
    @Schema(description = "이미지 카테고리", example = "REVIEW")
    ImageCategory category
) {

}
