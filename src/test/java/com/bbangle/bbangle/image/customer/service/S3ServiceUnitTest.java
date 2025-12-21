package com.bbangle.bbangle.image.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("[단위테스트] S3Service")
@Slf4j
class S3ServiceUnitTest {

    @Mock
    private AmazonS3 amazonS3;

    @InjectMocks
    private S3Service s3Service;

    private final String BUCKET_NAME = "test-bucket";
    private final String BUCKET_DOMAIN = "https://test-bucket.s3.amazonaws.com/";
    private final String CDN_DOMAIN = "https://cdn.example.com/";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucket", BUCKET_NAME);
        ReflectionTestUtils.setField(s3Service, "bucketDomain", BUCKET_DOMAIN);
        ReflectionTestUtils.setField(s3Service, "cdnDomain", CDN_DOMAIN);
    }

    @Test // 이 메서드가 JUnit 테스트 대상임을 명시합니다.
    @DisplayName("이미지를 S3에 업로드하고 CDN 도메인이 포함된 URL을 반환한다") // 테스트 실행 리포트에서 보여질 테스트의 목적을 설명합니다.
    void saveAndReturnWithCdn_uploadsImageAndReturnsCdnUrl() throws IOException {
        // given: 테스트를 위한 준비 단계 (데이터 및 Mock 설정)
        String folderName = "test-folder"; // 파일이 저장될 S3 내 폴더 경로
        String originalFileName = "test-image.jpg"; // 업로드할 원본 파일명
        byte[] content = "test image content".getBytes(); // 파일의 내용 (바이트 배열)

        // Spring의 MockMultipartFile을 사용하여 실제 HTTP 파일 업로드 객체를 흉내 냅니다.
        MultipartFile mockMultipartFile = new MockMultipartFile(
            "image",              // form field 이름
            originalFileName,     // 원본 파일명
            "image/jpg",          // 컨텐츠 타입
            new ByteArrayInputStream(content) // 파일 내용 스트림
        );

        // Mocking amazonS3.putObject
        // amazonS3.putObject() 메서드가 호출될 때, 실제 AWS에 요청하지 않고 Mock 객체(PutObjectResult)를 반환하도록 설정(Stubbing)합니다.
        when(amazonS3.putObject(any(PutObjectRequest.class))).thenReturn(mock(PutObjectResult.class));

        // Mocking amazonS3.getUrl
        // amazonS3.getUrl() 메서드 호출 시 동작을 정의합니다.
        when(amazonS3.getUrl(any(String.class), any(String.class)))
            .thenAnswer(invocation -> { // 인자에 따라 동적으로 결과를 반환하기 위해 thenAnswer 사용
                String bucket = invocation.getArgument(0); // 첫 번째 인자(버킷명) 추출
                String key = invocation.getArgument(1);    // 두 번째 인자(파일 키/경로) 추출
                // 테스트 시나리오상 S3 원본 도메인 URL을 반환한다고 가정 (서비스 로직에서 이를 CDN 도메인으로 교체하는지 확인하기 위함)
                return new URL(BUCKET_DOMAIN + key);
            });

        // when: 실제로 테스트할 비즈니스 로직 실행
        // 서비스의 메서드를 호출하여 업로드를 수행하고, 변환된 CDN URL을 반환받습니다.
        String resultUrl = s3Service.saveAndReturnWithCdn(folderName, mockMultipartFile);

        log.info("resultUrl={}", resultUrl); // 결과 URL을 로그로 출력하여 확인 (디버깅 용도)

        // then: 실행 결과 검증 단계

        // ArgumentCaptor를 생성하여 amazonS3.putObject 호출 시 전달된 인자(PutObjectRequest)를 가로챌 준비를 합니다.
        ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);

        // amazonS3.putObject가 실제로 호출되었는지 검증(verify)하고, 그때 사용된 인자를 captor에 저장합니다.
        verify(amazonS3).putObject(putObjectRequestCaptor.capture());

        // 캡처한 요청 객체(PutObjectRequest)를 가져옵니다.
        PutObjectRequest capturedRequest = putObjectRequestCaptor.getValue();

        // [요청 데이터 검증]
        // 1. 업로드 요청된 버킷 이름이 예상값과 일치하는지 검증
        assertThat(capturedRequest.getBucketName()).isEqualTo(BUCKET_NAME);
        // 2. 저장된 파일 키(경로)가 지정한 폴더명으로 시작하는지 검증
        assertThat(capturedRequest.getKey()).startsWith(folderName + "/");
        // 3. 저장된 파일 키(경로)가 올바른 확장자(.jpg)로 끝나는지 검증
        assertThat(capturedRequest.getKey()).endsWith(".jpg");
        // 4. 실제 업로드된 스트림의 내용이 원본 내용과 일치하는지 검증
        assertThat(capturedRequest.getInputStream().readAllBytes()).isEqualTo(content);
        // 5. 메타데이터의 컨텐츠 타입이 올바르게 설정되었는지 검증
        assertThat(capturedRequest.getMetadata().getContentType()).isEqualTo("image/jpg");

        // [반환 값 검증]
        // 1. 결과 URL이 S3 원본 도메인이 아닌, CDN 도메인으로 시작하는지 검증 (핵심 비즈니스 로직)
        assertThat(resultUrl).startsWith(CDN_DOMAIN + folderName + "/");
        // 2. 결과 URL의 확장자가 올바른지 검증
        assertThat(resultUrl).endsWith(".jpg");
        // 3. 결과 URL에 원본 S3 버킷 도메인이 포함되지 않았는지 재확인 (보안 및 성능 최적화 확인)
        assertThat(resultUrl).doesNotContain(BUCKET_DOMAIN);
    }

    @Test
    @DisplayName("유효하지 않은 이미지 파일인 경우 예외를 발생시킨다")
    void saveAndReturnWithCdn_throwsExceptionForInvalidImage() throws IOException {
        // given
        String folderName = "test-folder";
        String originalFileName = "test-document.pdf"; // Invalid image type
        byte[] content = "test document content".getBytes();
        MultipartFile mockMultipartFile = new MockMultipartFile(
            "document",
            originalFileName,
            "application/pdf",
            new ByteArrayInputStream(content)
        );

        // when & then
        assertThatThrownBy(() -> s3Service.saveAndReturnWithCdn(folderName, mockMultipartFile))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("지원하지 않는 형식의 파일입니다.");
    }
}
