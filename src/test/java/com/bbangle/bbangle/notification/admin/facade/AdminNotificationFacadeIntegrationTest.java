package com.bbangle.bbangle.notification.admin.facade;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bbangle.bbangle.admin.domain.Admin;
import com.bbangle.bbangle.admin.repository.AdminRepository;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationRequest.AdminNotificationCreateRequest;
import com.bbangle.bbangle.notification.admin.controller.dto.LinkDto;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeInfo.NoticeInfo;
import com.bbangle.bbangle.notification.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@DisplayName("[통합 테스트] AdminNotificationFacade")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AdminNotificationFacadeIntegrationTest {

    @Autowired
    private AdminNotificationFacade adminNotificationFacade;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private S3Service s3Service;

    private Long testAdminId;

    @BeforeEach
    void resetTable() {
        em.flush();
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE notice").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE admin").executeUpdate();
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

        // 테스트용 Admin 생성
        Admin testAdmin = Admin.builder()
            .accountId("test-admin")
            .password("test-password")
            .name("테스트 관리자")
            .build();
        testAdminId = adminRepository.save(testAdmin).getId();
        em.flush();
        em.clear();
    }


    @Test
    @DisplayName("관리자 공지사항 생성에 성공한다")
    void success_create_notification_with_images() {
        // given
        List<LinkDto> links = List.of(
            new LinkDto("https://example.com", "더보기"),
            new LinkDto("https://example2.com", "상세보기")
        );

        // MockMultipartFile을 사용하여 이미지 파일 생성
        MockMultipartFile image1 = new MockMultipartFile(
            "images",                       // 파라미터 이름
            "image1.jpg",                   // 원본 파일명
            MediaType.IMAGE_JPEG_VALUE,     // 컨텐츠 타입
            "image1 content".getBytes()     // 파일 내용
        );

        MockMultipartFile image2 = new MockMultipartFile(
            "images",
            "image2.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "image2 content".getBytes()
        );

        List<MultipartFile> images = List.of(image1, image2);

        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "공지사항 내용",
            links
        );

        // S3Service Mock 설정: 이미지 업로드 성공
        List<String> mockImageUrls = List.of(
            "https://cdn.example.com/image1.jpg",
            "https://cdn.example.com/image2.jpg"
        );
        when(s3Service.saveMultipleAndReturnWithCdn(anyString(), anyList()))
            .thenReturn(mockImageUrls);

        // when
        NoticeInfo noticeInfo = adminNotificationFacade.createNotice(testAdminId, request, images);

        // then
        assertThat(noticeInfo).isNotNull();
        assertThat(noticeInfo.id()).isNotNull();
        assertThat(noticeInfo.title()).isEqualTo("공지사항 제목");
        assertThat(noticeInfo.content()).isEqualTo("공지사항 내용");
        assertThat(noticeInfo.links()).hasSize(2);
        assertThat(noticeInfo.imageLinks()).hasSize(2);
        assertThat(noticeInfo.imageLinks()).containsExactlyElementsOf(mockImageUrls);

        // 링크 검증
        assertThat(noticeInfo.links().get(0).url()).isEqualTo("https://example.com");
        assertThat(noticeInfo.links().get(0).linkText()).isEqualTo("더보기");
        assertThat(noticeInfo.links().get(1).url()).isEqualTo("https://example2.com");
        assertThat(noticeInfo.links().get(1).linkText()).isEqualTo("상세보기");

        // S3Service 호출 검증
        verify(s3Service, times(1)).saveMultipleAndReturnWithCdn(anyString(), anyList());
    }

    @Test
    @DisplayName("공지사항 생성 실패 시 업로드된 이미지를 삭제한다")
    void rollback_uploaded_images_when_notification_creation_fails() {
        // given
        // 존재하지 않는 adminId 사용 -> FK 제약조건 위반 발생
        Long invalidAdminId = 99999L;

        List<LinkDto> links = List.of(
            new LinkDto("https://example.com", "더보기")
        );

        MockMultipartFile image1 = new MockMultipartFile(
            "images",
            "image1.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "image1 content".getBytes()
        );

        List<MultipartFile> images = List.of(image1);

        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "공지사항 내용",
            links
        );

        // S3Service Mock 설정: 이미지 업로드는 성공
        List<String> uploadedImageUrls = List.of(
            "https://cdn.example.com/image1.jpg"
        );
        when(s3Service.saveMultipleAndReturnWithCdn(anyString(), anyList()))
            .thenReturn(uploadedImageUrls);

        // when & then
        assertThatThrownBy(() ->
            adminNotificationFacade.createNotice(invalidAdminId, request, images)
        )
            .isInstanceOf(BbangleException.class);

        // 이미지 업로드는 호출되었지만
        verify(s3Service, times(1)).saveMultipleAndReturnWithCdn(anyString(), anyList());

        // 공지사항 생성 실패로 인해 이미지 삭제도 호출되어야 함
        verify(s3Service, times(1)).deleteImagesCdn(uploadedImageUrls);
    }
}
