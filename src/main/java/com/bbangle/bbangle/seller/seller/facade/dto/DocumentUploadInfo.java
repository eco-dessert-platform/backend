package com.bbangle.bbangle.seller.seller.facade.dto;

import org.springframework.web.multipart.MultipartFile;

public record DocumentUploadInfo(
    String type,
    MultipartFile file
) {
}
