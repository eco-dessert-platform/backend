package com.bbangle.bbangle.image.customer.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.s3.AmazonS3;
import com.bbangle.bbangle.config.S3IntegrationTestSupport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@DisplayName("[통합테스트] SellerIntegrationTest")
@Slf4j
class S3ServiceIntegrationTest extends S3IntegrationTestSupport {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cdn.domain}")
    private String cdnDomain;

    @AfterEach
    void tearDown() {
        // LocalStack은 컨테이너가 유지되는 동안 데이터가 남으므로
        // 다음 테스트를 위해 반드시 비워주는 것이 좋습니다.
        var objectList = amazonS3.listObjects(bucket).getObjectSummaries();
        for (var objectSummary : objectList) {
            amazonS3.deleteObject(bucket, objectSummary.getKey());
        }
    }

    @Test
    @DisplayName("이미지를 S3에 업로드하면, LocalStack S3에 저장되고 CDN 도메인 URL이 반환된다")
    void saveAndReturnWithCdn_uploadsImageAndReturnsCdnUrl() throws IOException {
        // given
        String folderName = "integration-test";
        String originalFileName = "test-image.png";
        byte[] content = "test image content".getBytes();

        MultipartFile mockMultipartFile = new MockMultipartFile(
            "image",
            originalFileName,
            "image/png",
            new ByteArrayInputStream(content)
        );

        // when
        String resultUrl = s3Service.saveAndReturnWithCdn(folderName, mockMultipartFile);

        log.info("resultUrl = {}", resultUrl);

        // then
        // 1. URL 형식 검증
        assertThat(resultUrl)
            .startsWith(cdnDomain)
            .doesNotContain("127.0.0.1") // LocalStack 주소는 제거되었어야 함
            .contains(folderName)
            .endsWith(".png");

        // 2. 실제 S3 조회용 Key 추출
        // URL에서 cdnDomain만 빼면 바로 Key가 됨
        String objectKey = resultUrl.replace(cdnDomain, "");

        // 슬래시 처리
        if (objectKey.startsWith("/")) {
            objectKey = objectKey.substring(1);
        }

        log.info("objectKey = {}", objectKey);

        // 3. S3 저장 검증
        boolean objectExists = amazonS3.doesObjectExist(bucket, objectKey);
        assertThat(objectExists).isTrue();
    }
}
