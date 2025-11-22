package com.bbangle.bbangle.config;


import com.amazonaws.services.s3.AmazonS3;
import com.bbangle.bbangle.image.customer.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
@ActiveProfiles("test")
public class S3IntegrationTestSupport {

    @Autowired
    protected S3Service s3Service;

    @Autowired
    protected AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    protected String bucket;

    /**
     * LocalStack은 실행될 때마다 포트가 바뀌므로, bucketDomain 값이 일치하지 않습니다. <br> 따라서 테스트 시작 전에
     * 'ReflectionTestUtils'를 사용해 S3Service 내부의 bucketDomain 값을 현재 실행 중인 LocalStack의 Endpoint로 강제
     * 주입합니다.
     */
    @BeforeEach
    void globalSetUp() {
        // LocalStack URL 동기화 로직을 여기서 한 번만 작성
        // 1. 현재 실행 중인 LocalStack S3의 기본 URL 가져오기 => bucketDomain 역할.
        // 예: http://127.0.0.1:65234/bbangree-test-s3
        String localStackUrl = amazonS3.getUrl(bucket, "").toString();
        // 끝에 슬래시가 있다면 제거 (S3Service 로직에 따라 조정)
        if (localStackUrl.endsWith("/")) {
            localStackUrl = localStackUrl.substring(0, localStackUrl.length() - 1);
        }
        // 자식 클래스들이 s3Service를 사용할 때 자동으로 적용됨
        // 2. S3Service의 private 필드 'bucketDomain'을 강제로 변경
        ReflectionTestUtils.setField(s3Service, "bucketDomain", localStackUrl);
    }
}
