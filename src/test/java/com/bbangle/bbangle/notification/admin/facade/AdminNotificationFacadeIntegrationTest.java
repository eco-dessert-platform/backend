package com.bbangle.bbangle.notification.admin.facade;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bbangle.bbangle.admin.domain.Admin;
import com.bbangle.bbangle.admin.repository.AdminRepository;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationRequest.AdminNotificationCreateRequest;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationRequest.AdminNotificationUpdateRequest;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeInfo.NoticeInfo;
import com.bbangle.bbangle.notification.domain.Notice;
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
        // MockMultipartFile을 사용하여 이미지 파일 생성
        MockMultipartFile image1 = new MockMultipartFile(
            "images",                       // 파라미터 이름
            "uuid-1",                       // uuid 파일명
            MediaType.IMAGE_JPEG_VALUE,     // 컨텐츠 타입
            "image1 content".getBytes()     // 파일 내용
        );

        MockMultipartFile image2 = new MockMultipartFile(
            "images",
            "uuid-2",
            MediaType.IMAGE_JPEG_VALUE,
            "image2 content".getBytes()
        );

        List<MultipartFile> images = List.of(image1, image2);

        String htmlContent = "<div><img src=\"uuid-1\"><img src=\"uuid-2\"></div>";
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            htmlContent
        );

        // S3Service Mock 설정: 각 이미지 업로드 성공
        String mockImageUrl1 = "https://cdn.example.com/image1.jpg";
        String mockImageUrl2 = "https://cdn.example.com/image2.jpg";
        when(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(image1)))
            .thenReturn(mockImageUrl1);
        when(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(image2)))
            .thenReturn(mockImageUrl2);

        // when
        NoticeInfo noticeInfo = adminNotificationFacade.createNotice(testAdminId, request, images);

        // then
        assertThat(noticeInfo).isNotNull();
        assertThat(noticeInfo.id()).isNotNull();
        assertThat(noticeInfo.title()).isEqualTo("공지사항 제목");
        assertThat(noticeInfo.imageLinks()).hasSize(2);
        assertThat(noticeInfo.imageLinks()).containsExactly(mockImageUrl1, mockImageUrl2);

        // S3Service 호출 검증
        verify(s3Service, times(1)).saveAndReturnWithCdn(eq("admin-notice-images"), eq(image1));
        verify(s3Service, times(1)).saveAndReturnWithCdn(eq("admin-notice-images"), eq(image2));
    }

    @Test
    @DisplayName("공지사항 생성 실패 시 업로드된 이미지를 삭제한다")
    void rollback_uploaded_images_when_notification_creation_fails() {
        // given
        // 존재하지 않는 adminId 사용 -> FK 제약조건 위반 발생
        Long invalidAdminId = 99999L;

        MockMultipartFile image1 = new MockMultipartFile(
            "images",
            "uuid-1",
            MediaType.IMAGE_JPEG_VALUE,
            "image1 content".getBytes()
        );

        List<MultipartFile> images = List.of(image1);

        String htmlContent = "<div><img src=\"uuid-1\"></div>";
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            htmlContent
        );

        // S3Service Mock 설정: 이미지 업로드는 성공
        String uploadedImageUrl = "https://cdn.example.com/image1.jpg";
        List<String> uploadedImageUrls = List.of(uploadedImageUrl);
        when(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(image1)))
            .thenReturn(uploadedImageUrl);

        // when & then
        assertThatThrownBy(() ->
            adminNotificationFacade.createNotice(invalidAdminId, request, images)
        )
            .isInstanceOf(BbangleException.class);

        // 이미지 업로드는 호출되었지만
        verify(s3Service, times(1)).saveAndReturnWithCdn(eq("admin-notice-images"), eq(image1));

        // 공지사항 생성 실패로 인해 이미지 삭제도 호출되어야 함
        verify(s3Service, times(1)).deleteImagesCdn(uploadedImageUrls);
    }

    @Test
    @DisplayName("복수 이미지 업로드 중 첫 번째 이미지만 성공할 때 첫 번째 이미지만 삭제된다")
    void rollback_first_image_when_second_image_fails() {
        // given
        MockMultipartFile image1 = new MockMultipartFile(
            "images",
            "uuid-1",
            MediaType.IMAGE_JPEG_VALUE,
            "image1 content".getBytes()
        );

        MockMultipartFile image2 = new MockMultipartFile(
            "images",
            "uuid-2",
            MediaType.IMAGE_JPEG_VALUE,
            "image2 content".getBytes()
        );

        List<MultipartFile> images = List.of(image1, image2);

        String htmlContent = "<div><img src=\"uuid-1\"><img src=\"uuid-2\"></div>";
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            htmlContent
        );

        // 첫 번째 이미지는 성공
        String uploadedImageUrl1 = "https://cdn.example.com/image1.jpg";
        when(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(image1)))
            .thenReturn(uploadedImageUrl1);

        // 두 번째 이미지는 실패
        when(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(image2)))
            .thenThrow(new RuntimeException("S3 upload failed for second image"));

        // when & then
        assertThatThrownBy(() ->
            adminNotificationFacade.createNotice(testAdminId, request, images)
        )
            .isInstanceOf(BbangleException.class);

        // 첫 번째 이미지만 삭제되어야 함
        verify(s3Service, times(1)).deleteImagesCdn(List.of(uploadedImageUrl1));
    }

    @Test
    @DisplayName("이미지 없이 공지사항 생성에 성공한다")
    void success_create_notification_without_images() {
        // given
        MockMultipartFile noImages = new MockMultipartFile(
            "images",
            "".getBytes()
        );

        List<MultipartFile> emptyImages = List.of();

        String htmlContent = "<div>텍스트만 있는 공지사항</div>";
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            htmlContent
        );

        // when
        NoticeInfo noticeInfo = adminNotificationFacade.createNotice(testAdminId, request, emptyImages);

        // then
        assertThat(noticeInfo).isNotNull();
        assertThat(noticeInfo.title()).isEqualTo("공지사항 제목");
        assertThat(noticeInfo.imageLinks()).isEmpty();

        // S3Service는 호출되지 않아야 함
        verify(s3Service, times(0)).saveAndReturnWithCdn(anyString(), any());
    }

    @Test
    @DisplayName("이미지 없이 공지사항 제목과 본문만 수정에 성공한다")
    void success_update_notice_without_images() {
        // given
        // 먼저 공지사항 생성
        NoticeInfo createdNotice = adminNotificationFacade.createNotice(
            testAdminId,
            new AdminNotificationCreateRequest("원본 제목", "원본 내용"),
            List.of()
        );
        em.flush();
        em.clear();

        AdminNotificationUpdateRequest request = new AdminNotificationUpdateRequest(
            "수정된 제목",
            "수정된 내용"
        );

        // when
        NoticeInfo updatedNotice = adminNotificationFacade.updateNotice(
            testAdminId,
            createdNotice.id(),
            request,
            List.of()
        );

        // then
        assertThat(updatedNotice).isNotNull();
        assertThat(updatedNotice.id()).isEqualTo(createdNotice.id());
        assertThat(updatedNotice.title()).isEqualTo("수정된 제목");
        assertThat(updatedNotice.content()).isEqualTo("수정된 내용");
        assertThat(updatedNotice.imageLinks()).isEmpty();

        // DB에서 직접 확인
        Notice notice = notificationRepository.findById(createdNotice.id()).orElseThrow();
        assertThat(notice.getTitle()).isEqualTo("수정된 제목");
        assertThat(notice.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("새로운 이미지를 추가하여 공지사항 수정에 성공한다")
    void success_update_notice_with_new_images() {
        // given
        // 먼저 이미지 없는 공지사항 생성
        NoticeInfo createdNotice = adminNotificationFacade.createNotice(
            testAdminId,
            new AdminNotificationCreateRequest("원본 제목", "원본 내용"),
            List.of()
        );
        em.flush();
        em.clear();

        MockMultipartFile newImage = new MockMultipartFile(
            "images",
            "uuid-1",
            MediaType.IMAGE_JPEG_VALUE,
            "new image content".getBytes()
        );
        List<MultipartFile> images = List.of(newImage);

        String newImageUrl = "https://cdn.example.com/new-image.jpg";
        when(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(newImage)))
            .thenReturn(newImageUrl);

        AdminNotificationUpdateRequest request = new AdminNotificationUpdateRequest(
            "수정된 제목",
            "<div><img src=\"uuid-1\"></div>"
        );

        // when
        NoticeInfo updatedNotice = adminNotificationFacade.updateNotice(
            testAdminId,
            createdNotice.id(),
            request,
            images
        );

        // then
        assertThat(updatedNotice).isNotNull();
        assertThat(updatedNotice.imageLinks()).hasSize(1);
        // HTML 컨텐츠에서 추출된 실제 이미지 링크 확인
        assertThat(updatedNotice.content()).contains("uuid-1");

        // S3 호출 검증
        verify(s3Service, times(1)).saveAndReturnWithCdn(eq("admin-notice-images"), eq(newImage));
    }

    @Test
    @DisplayName("기존 이미지를 삭제하고 공지사항 수정에 성공한다")
    void success_update_notice_removing_existing_images() {
        // given
        // 먼저 이미지 있는 공지사항 생성
        MockMultipartFile originalImage = new MockMultipartFile(
            "images",
            "uuid-original",
            MediaType.IMAGE_JPEG_VALUE,
            "original image".getBytes()
        );

        String originalImageUrl = "https://cdn.example.com/original-image.jpg";
        when(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(originalImage)))
            .thenReturn(originalImageUrl);

        NoticeInfo createdNotice = adminNotificationFacade.createNotice(
            testAdminId,
            new AdminNotificationCreateRequest("원본 제목", "<div><img src=\"uuid-original\"></div>"),
            List.of(originalImage)
        );
        em.flush();
        em.clear();

        AdminNotificationUpdateRequest request = new AdminNotificationUpdateRequest(
            "수정된 제목",
            "<div>이미지 삭제됨</div>"
        );

        // when
        NoticeInfo updatedNotice = adminNotificationFacade.updateNotice(
            testAdminId,
            createdNotice.id(),
            request,
            List.of()
        );

        // then
        assertThat(updatedNotice).isNotNull();
        assertThat(updatedNotice.content()).isEqualTo("<div>이미지 삭제됨</div>");

        // 이미지가 제거되었는지 확인
        assertThat(updatedNotice.content()).doesNotContain("uuid-original");
    }

    @Test
    @DisplayName("기존 이미지를 새 이미지로 교체하여 수정에 성공한다")
    void success_update_notice_replacing_images() {
        // given
        // 먼저 이미지 있는 공지사항 생성
        MockMultipartFile originalImage = new MockMultipartFile(
            "images",
            "uuid-original",
            MediaType.IMAGE_JPEG_VALUE,
            "original image".getBytes()
        );

        String originalImageUrl = "https://cdn.example.com/original-image.jpg";
        when(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(originalImage)))
            .thenReturn(originalImageUrl);

        NoticeInfo createdNotice = adminNotificationFacade.createNotice(
            testAdminId,
            new AdminNotificationCreateRequest("원본 제목", "<div><img src=\"uuid-original\"></div>"),
            List.of(originalImage)
        );
        em.flush();
        em.clear();

        // 새 이미지로 교체
        MockMultipartFile newImage = new MockMultipartFile(
            "images",
            "uuid-new",
            MediaType.IMAGE_JPEG_VALUE,
            "new image".getBytes()
        );

        String newImageUrl = "https://cdn.example.com/new-image.jpg";
        when(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(newImage)))
            .thenReturn(newImageUrl);

        AdminNotificationUpdateRequest request = new AdminNotificationUpdateRequest(
            "수정된 제목",
            "<div><img src=\"uuid-new\"></div>"
        );

        // when
        NoticeInfo updatedNotice = adminNotificationFacade.updateNotice(
            testAdminId,
            createdNotice.id(),
            request,
            List.of(newImage)
        );

        // then
        assertThat(updatedNotice).isNotNull();
        assertThat(updatedNotice.imageLinks()).hasSize(1);
        assertThat(updatedNotice.content()).contains("uuid-new");
        assertThat(updatedNotice.content()).doesNotContain("uuid-original");

        // S3 호출 검증
        verify(s3Service, times(1)).saveAndReturnWithCdn(eq("admin-notice-images"), eq(newImage));
        // S3에서 기존 이미지 삭제 호출되었는지 확인
        verify(s3Service, times(1)).deleteImagesCdn(List.of(originalImageUrl));
    }

    @Test
    @DisplayName("DB 업데이트 실패 시 새로 업로드된 이미지를 롤백한다")
    void rollback_new_images_when_update_fails() {
        // given
        // 먼저 공지사항 생성
        NoticeInfo createdNotice = adminNotificationFacade.createNotice(
            testAdminId,
            new AdminNotificationCreateRequest("원본 제목", "원본 내용"),
            List.of()
        );
        em.flush();
        em.clear();

        // 존재하지 않는 noticeId로 수정 시도
        Long invalidNoticeId = 99999L;

        MockMultipartFile newImage = new MockMultipartFile(
            "images",
            "uuid-1",
            MediaType.IMAGE_JPEG_VALUE,
            "new image".getBytes()
        );

        String newImageUrl = "https://cdn.example.com/new-image.jpg";
        when(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(newImage)))
            .thenReturn(newImageUrl);

        AdminNotificationUpdateRequest request = new AdminNotificationUpdateRequest(
            "수정된 제목",
            "<div><img src=\"uuid-1\"></div>"
        );

        // when & then
        assertThatThrownBy(() ->
            adminNotificationFacade.updateNotice(
                testAdminId,
                invalidNoticeId,
                request,
                List.of(newImage)
            )
        ).isInstanceOf(BbangleException.class);

        // 새로 업로드된 이미지는 삭제되어야 함
        verify(s3Service, times(1)).deleteImagesCdn(List.of(newImageUrl));
    }
}
