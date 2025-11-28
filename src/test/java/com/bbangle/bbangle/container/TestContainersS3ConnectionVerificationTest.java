package com.bbangle.bbangle.container;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.localstack.LocalStackContainer;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("LocalStack 연결 및 S3 기능 검증 테스트")
@Slf4j
public class TestContainersS3ConnectionVerificationTest {

    @Autowired
    private LocalStackContainer localStackContainer;

    @Autowired
    private AmazonS3 amazonS3;

    @Test
    @Order(1)
    @DisplayName("1. LocalStack 컨테이너가 Docker에서 정상적으로 실행 중이어야 한다")
    void localStackIsRunning() {
        // given & when
        boolean isRunning = localStackContainer.isRunning();
        String endpoint = localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3)
            .toString();
        String region = localStackContainer.getRegion();

        log.info("✅ LocalStack Endpoint: " + endpoint);
        log.info("✅ LocalStack Region: " + region);

        // then
        assertThat(isRunning).isTrue();
        assertThat(endpoint).contains("127.0.0.1")
            .as("로컬 루프백 주소를 가리켜야 함");
    }

    @Test
    @Order(2)
    @DisplayName("2. AmazonS3 클라이언트를 통해 버킷 생성, 파일 업로드/다운로드가 가능해야 한다")
    void s3CrudOperations() {
        // given
        String bucketName = "connection-check-bucket";
        String fileName = "hello.txt";
        String content = "Hello LocalStack!";

        // when: 버킷 생성
        if (!amazonS3.doesBucketExistV2(bucketName)) {
            amazonS3.createBucket(bucketName);
        }

        // then: 버킷 존재 확인
        assertThat(amazonS3.doesBucketExistV2(bucketName)).isTrue();

        // when: 파일 업로드
        amazonS3.putObject(bucketName, fileName, content);

        // then: 파일 존재 확인
        assertThat(amazonS3.doesObjectExist(bucketName, fileName)).isTrue();

        // when: 파일 다운로드 및 내용 검증
        S3Object s3Object = amazonS3.getObject(bucketName, fileName);
        String downloadedContent = new BufferedReader(
            new InputStreamReader(s3Object.getObjectContent(), StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining("\n"));

        assertThat(downloadedContent).isEqualTo(content);

        // cleanup (테스트 후 정리)
        amazonS3.deleteObject(bucketName, fileName);
        amazonS3.deleteBucket(bucketName);
    }

}
