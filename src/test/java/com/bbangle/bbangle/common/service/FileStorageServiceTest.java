package com.bbangle.bbangle.common.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.amazonaws.AmazonServiceException;
import com.bbangle.bbangle.exception.BbangleException;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.amazonaws.services.s3.AmazonS3;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@AutoConfigureMockMvc
class FileStorageServiceTest {
    @Mock
    private AmazonS3 amazonS3;

    @InjectMocks
    private FileStorageServiceImpl fileStorageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(fileStorageService, "bucketName", "test-bucket");
    }

    @Test
    @DisplayName("파일 업로드 시 유효성을 검증할 수 있다")
    void validFileUploading() {
        // Arrange
        InputStream file = new ByteArrayInputStream("test-data".getBytes());
        String savedUrl = "test/path/file.txt";
        String contentType = "text/plain";
        long contentLength = 1024L;

        when(amazonS3.doesObjectExist("test-bucket", savedUrl)).thenReturn(false);
        when(amazonS3.doesObjectExist(anyString(), anyString())).thenReturn(true);
        // Act & Assert
        assertThatCode(() -> fileStorageService.uploadStreamFile(file, savedUrl, contentType, contentLength))
            .doesNotThrowAnyException();

        verify(amazonS3, times(1)).doesObjectExist("test-bucket", savedUrl);
    }

    @Test
    @DisplayName("S3에 파일이 존재하지 않으면 예외를 발생시킬 수 있다")
    void getFile_FileNotFound() {
        String savedUrl = "nonexistent/file.txt";
        Mockito.when(amazonS3.doesObjectExist(Mockito.anyString(), Mockito.eq(savedUrl))).thenReturn(false);

        Assertions.assertThrows(BbangleException.class, () -> fileStorageService.getFile(savedUrl));
    }

    @Test
    @DisplayName("AWS 서비스 예외를 처리할 수 있다")
    void getFile_AwsError() {
        String savedUrl = "test/file.txt";
        Mockito.when(amazonS3.getObject(Mockito.anyString(), Mockito.eq(savedUrl)))
            .thenThrow(AmazonServiceException.class);

        Assertions.assertThrows(BbangleException.class, () -> fileStorageService.getFile(savedUrl));
    }

    @Test
    @DisplayName("S3에 파일을 성공적으로 업로드할 수 있다")
    void uploadStreamFile_Success() {
        String savedUrl = "test/file.txt";
        InputStream file = new ByteArrayInputStream("Test content".getBytes());
        String contentType = "text/plain";
        long contentLength = 11;

        when(amazonS3.doesObjectExist(anyString(), anyString())).thenReturn(true);
        Mockito.doAnswer(invocation -> null).when(amazonS3)
            .putObject(Mockito.anyString(), Mockito.eq(savedUrl), Mockito.eq(file), Mockito.any());

        fileStorageService.uploadStreamFile(file, savedUrl, contentType, contentLength);
        Mockito.verify(amazonS3, Mockito.times(1)).putObject(Mockito.anyString(), Mockito.eq(savedUrl), Mockito.any(), Mockito.any());
    }

    @Test
    @DisplayName("파일이 null이면 예외를 발생시킬 수 있다")
    void uploadStreamFile_NullFile() {
        Assertions.assertThrows(BbangleException.class, () -> {
            fileStorageService.uploadStreamFile(null, "test/file.txt", "text/plain", 10);
        });
    }

    @Test
    @DisplayName("contentType이 비어있으면 예외를 발생시킬 수 있다")
    void uploadStreamFile_EmptyContentType() {
        InputStream file = new ByteArrayInputStream("Test content".getBytes());
        Assertions.assertThrows(BbangleException.class, () -> {
            fileStorageService.uploadStreamFile(file, "test/file.txt", "", 10);
        });
    }

    @Test
    @DisplayName("파일 크기가 유효하지 않으면 예외를 발생시킬 수 있다")
    void uploadStreamFile_InvalidContentLength() {
        InputStream file = new ByteArrayInputStream("Test content".getBytes());
        Assertions.assertThrows(BbangleException.class, () -> {
            fileStorageService.uploadStreamFile(file, "test/file.txt", "text/plain", 0);
        });
    }

    @Test
    @DisplayName("업로드 중 AWS 예외를 처리할 수 있다")
    void uploadStreamFile_AwsError() {
        String savedUrl = "test/file.txt";
        InputStream file = new ByteArrayInputStream("Test content".getBytes());
        String contentType = "text/plain";
        long contentLength = 11;

        Mockito.doThrow(AmazonServiceException.class).when(amazonS3)
            .putObject(Mockito.anyString(), Mockito.eq(savedUrl), Mockito.eq(file), Mockito.any());

        Assertions.assertThrows(BbangleException.class, () -> {
            fileStorageService.uploadStreamFile(file, savedUrl, contentType, contentLength);
        });
    }

    @Test
    @DisplayName("파일이 존재하지 않으면 유효성 검증 중 예외를 발생시킬 수 있다")
    void validateFileExists_FileNotFound() {
        String savedUrl = "nonexistent/file.txt";
        Mockito.when(amazonS3.doesObjectExist(Mockito.anyString(), Mockito.eq(savedUrl))).thenReturn(false);

        Assertions.assertThrows(BbangleException.class, () -> fileStorageService.getFile(savedUrl));
    }

    @Test
    @DisplayName("업로드 실패 시 파일 스트림을 닫을 수 있다")
    void closeStreamOnFailure() {
        String savedUrl = "test/file.txt";
        InputStream file = Mockito.spy(new ByteArrayInputStream("Test content".getBytes()));
        String contentType = "text/plain";
        long contentLength = 11;

        when(amazonS3.doesObjectExist(anyString(), anyString())).thenReturn(true);
        Mockito.doThrow(AmazonServiceException.class).when(amazonS3)
            .putObject(Mockito.anyString(), Mockito.eq(savedUrl), Mockito.eq(file), Mockito.any());

        Assertions.assertThrows(BbangleException.class, () -> {
            fileStorageService.uploadStreamFile(file, savedUrl, contentType, contentLength);
        });

        try {
            Mockito.verify(file, Mockito.times(1)).close();
        } catch (IOException e) {
            Assertions.fail("Stream should have been closed");
        }
    }


}
