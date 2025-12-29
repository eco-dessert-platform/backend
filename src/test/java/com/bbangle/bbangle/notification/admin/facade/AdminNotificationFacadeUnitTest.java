package com.bbangle.bbangle.notification.admin.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.notification.admin.controller.dto.LinkDto;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationRequest.AdminNotificationCreateRequest;
import com.bbangle.bbangle.notification.admin.service.AdminNotificationService;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeCommand.AdminNoticeCreateCommand;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeInfo.NoticeInfo;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@DisplayName("[단위 테스트] AdminNotificationFacade")
@ExtendWith(MockitoExtension.class)
class AdminNotificationFacadeUnitTest {

    @InjectMocks
    private AdminNotificationFacade sut;

    @Mock
    private S3Service s3Service;

    @Mock
    private AdminNotificationService adminNotificationService;

    @Test
    @DisplayName("이미지 업로드와 공지사항 생성에 성공한다")
    void success_create_notice_with_images() {
        // given
        Long adminId = 1L;
        List<LinkDto> links = List.of(
            new LinkDto("https://example.com", "더보기"),
            new LinkDto("https://example2.com", "상세보기")
        );
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "공지사항 내용",
            links
        );

        MockMultipartFile image1 = new MockMultipartFile(
            "images",
            "test1.jpg",
            "image/jpeg",
            "test image content 1".getBytes()
        );
        MockMultipartFile image2 = new MockMultipartFile(
            "images",
            "test2.jpg",
            "image/jpeg",
            "test image content 2".getBytes()
        );
        List<MultipartFile> profileImages = List.of(image1, image2);

        List<String> uploadedImageLinks = List.of(
            "https://cdn.example.com/admin-notice-images/image1.jpg",
            "https://cdn.example.com/admin-notice-images/image2.jpg"
        );

        NoticeInfo expectedNoticeInfo = new NoticeInfo(
            1L,
            "공지사항 제목",
            "공지사항 내용",
            links,
            uploadedImageLinks,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        given(s3Service.saveMultipleAndReturnWithCdn(eq("admin-notice-images"), eq(profileImages)))
            .willReturn(uploadedImageLinks);
        given(adminNotificationService.createAdminNotification(any(AdminNoticeCreateCommand.class)))
            .willReturn(expectedNoticeInfo);

        // when
        NoticeInfo result = sut.createNotice(adminId, request, profileImages);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("공지사항 제목");
        assertThat(result.content()).isEqualTo("공지사항 내용");

        then(s3Service).should(times(1))
            .saveMultipleAndReturnWithCdn(eq("admin-notice-images"), eq(profileImages));
        then(adminNotificationService).should(times(1))
            .createAdminNotification(any(AdminNoticeCreateCommand.class));
        then(s3Service).should(never()).deleteImages(anyList());
    }

    @Test
    @DisplayName("공지사항 생성 실패 시 업로드된 이미지를 삭제한다")
    void fail_create_notice_then_delete_uploaded_images() {
        // given
        Long adminId = 1L;
        List<LinkDto> links = List.of(new LinkDto("https://example.com", "더보기"));
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "공지사항 내용",
            links
        );

        MockMultipartFile image = new MockMultipartFile(
            "images",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
        List<MultipartFile> profileImages = List.of(image);

        List<String> uploadedImageLinks = List.of(
            "https://cdn.example.com/admin-notice-images/image1.jpg"
        );

        given(s3Service.saveMultipleAndReturnWithCdn(eq("admin-notice-images"), eq(profileImages)))
            .willReturn(uploadedImageLinks);
        given(adminNotificationService.createAdminNotification(any(AdminNoticeCreateCommand.class)))
            .willThrow(new BbangleException(BbangleErrorCode.ADMIN_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> sut.createNotice(adminId, request, profileImages))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.ADMIN_NOTICE_CREATION_FAILED.getMessage());

        then(s3Service).should(times(1))
            .saveMultipleAndReturnWithCdn(eq("admin-notice-images"), eq(profileImages));
        then(adminNotificationService).should(times(1))
            .createAdminNotification(any(AdminNoticeCreateCommand.class));
        then(s3Service).should(times(1))
            .deleteImagesCdn(eq(uploadedImageLinks));
    }

    @Test
    @DisplayName("제목이 비어있는 경우 실패하고 이미지를 삭제한다")
    void fail_create_notice_when_title_is_empty_then_delete_images() {
        // given
        Long adminId = 1L;
        List<LinkDto> links = List.of(new LinkDto("https://example.com", "더보기"));
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "",  // 빈 제목
            "공지사항 내용",
            links
        );

        MockMultipartFile image = new MockMultipartFile(
            "images",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
        List<MultipartFile> profileImages = List.of(image);

        List<String> uploadedImageLinks = List.of(
            "https://cdn.example.com/admin-notice-images/image1.jpg"
        );

        given(s3Service.saveMultipleAndReturnWithCdn(eq("admin-notice-images"), eq(profileImages)))
            .willReturn(uploadedImageLinks);
        given(adminNotificationService.createAdminNotification(any(AdminNoticeCreateCommand.class)))
            .willThrow(new BbangleException(BbangleErrorCode.TITLE_IS_EMPTY));

        // when & then
        assertThatThrownBy(() -> sut.createNotice(adminId, request, profileImages))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.ADMIN_NOTICE_CREATION_FAILED.getMessage());

        then(s3Service).should(times(1))
            .deleteImagesCdn(eq(uploadedImageLinks));
    }

    @Test
    @DisplayName("존재하지 않는 Admin ID로 공지사항 생성 시 실패하고 이미지를 삭제한다")
    void fail_create_notice_when_admin_not_found_then_delete_images() {
        // given
        Long nonExistentAdminId = 999L;
        List<LinkDto> links = List.of(new LinkDto("https://example.com", "더보기"));
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "공지사항 내용",
            links
        );

        MockMultipartFile image = new MockMultipartFile(
            "images",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
        List<MultipartFile> profileImages = List.of(image);

        List<String> uploadedImageLinks = List.of(
            "https://cdn.example.com/admin-notice-images/image1.jpg"
        );

        given(s3Service.saveMultipleAndReturnWithCdn(eq("admin-notice-images"), eq(profileImages)))
            .willReturn(uploadedImageLinks);
        given(adminNotificationService.createAdminNotification(any(AdminNoticeCreateCommand.class)))
            .willThrow(new BbangleException(BbangleErrorCode.ADMIN_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> sut.createNotice(nonExistentAdminId, request, profileImages))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.ADMIN_NOTICE_CREATION_FAILED.getMessage());

        then(s3Service).should(times(1))
            .saveMultipleAndReturnWithCdn(eq("admin-notice-images"), eq(profileImages));
        then(adminNotificationService).should(times(1))
            .createAdminNotification(any(AdminNoticeCreateCommand.class));
        then(s3Service).should(times(1))
            .deleteImagesCdn(eq(uploadedImageLinks));
    }

    @Test
    @DisplayName("빈 이미지 리스트로도 공지사항 생성에 성공한다")
    void success_create_notice_with_empty_images() {
        // given
        Long adminId = 1L;
        List<LinkDto> links = List.of(new LinkDto("https://example.com", "더보기"));
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "공지사항 내용",
            links
        );

        List<MultipartFile> emptyImages = List.of();
        List<String> emptyImageLinks = List.of();

        NoticeInfo expectedNoticeInfo = new NoticeInfo(
            1L,
            "공지사항 제목",
            "공지사항 내용",
            links,
            emptyImageLinks,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        given(s3Service.saveMultipleAndReturnWithCdn(eq("admin-notice-images"), eq(emptyImages)))
            .willReturn(emptyImageLinks);
        given(adminNotificationService.createAdminNotification(any(AdminNoticeCreateCommand.class)))
            .willReturn(expectedNoticeInfo);

        // when
        NoticeInfo result = sut.createNotice(adminId, request, emptyImages);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("공지사항 제목");

        then(s3Service).should(times(1))
            .saveMultipleAndReturnWithCdn(eq("admin-notice-images"), eq(emptyImages));
        then(adminNotificationService).should(times(1))
            .createAdminNotification(any(AdminNoticeCreateCommand.class));
        then(s3Service).should(never()).deleteImages(anyList());
    }
}
