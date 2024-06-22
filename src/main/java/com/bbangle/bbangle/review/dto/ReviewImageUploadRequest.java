package com.bbangle.bbangle.review.dto;

import com.bbangle.bbangle.image.domain.ImageCategory;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ReviewImageUploadRequest(
        List<MultipartFile> images,
        ImageCategory category
) {
}
