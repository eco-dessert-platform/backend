package com.bbangle.bbangle.image.customer.validation;

import static io.jsonwebtoken.lang.Assert.isTrue;
import static io.jsonwebtoken.lang.Assert.notNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageValidator {

    private static final String IMAGE_PREFIX = "image";
    private static final String EMPTY_FILE = "비어있는 파일은 업로드 할 수 없습니다.";
    private static final String EMPTY_FILE_NAME = "파일명이 없습니다";
    private static final String FILE_OVER_SIZE = "파일이 허용된 최대 크기를 초과했습니다.";
    private static final String INVALID_FILE_TYPE = "지원하지 않는 형식의 파일입니다.";

    public static void validateImage(MultipartFile img) {
        notNull(img, EMPTY_FILE);
        notNull(img.getOriginalFilename(), EMPTY_FILE_NAME);
        isTrue(!img.isEmpty(), EMPTY_FILE);
        isTrue(img.getSize() < 10_000_000, FILE_OVER_SIZE);
        if (!isBlank(img.getContentType())) {
            isTrue(img.getContentType().startsWith(IMAGE_PREFIX), INVALID_FILE_TYPE);
        }
    }

}
