package com.bbangle.bbangle.notification.admin.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationRequest.AdminNotificationCreateRequest;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationRequest.AdminNotificationUpdateRequest;
import com.bbangle.bbangle.notification.admin.service.AdminNotificationService;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeCommand.AdminNoticeCreateCommand;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeCommand.AdminNoticeUpdateCommand;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeInfo.NoticeInfo;
import com.bbangle.bbangle.common.service.HtmlContentProcessor;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Mock
    private HtmlContentProcessor htmlContentProcessor;

    @Test
    @DisplayName("단일 이미지로 공지사항 생성에 성공한다")
    void success_create_notice_with_single_image() {
        // given
        Long adminId = 1L;
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "공지사항 내용"
        );

        MockMultipartFile image = new MockMultipartFile(
            "images",
            "uuid-1",
            "image/jpeg",
            "test image content".getBytes()
        );
        List<MultipartFile> images = List.of(image);

        String uploadedImageLink = "https://cdn.example.com/admin-notice-images/image1.jpg";
        Map<String, String> expectedCdnUrlMap = new HashMap<>();
        expectedCdnUrlMap.put("uuid-1", uploadedImageLink);

        given(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(image)))
            .willReturn(uploadedImageLink);
        given(htmlContentProcessor.changeToCdn(anyString(), any(Map.class)))
            .willReturn("변환된 내용");
        given(adminNotificationService.createAdminNotification(any(AdminNoticeCreateCommand.class)))
            .willReturn(NoticeInfo.builder()
                .id(1L)
                .title("공지사항 제목")
                .content("변환된 내용")
                .imageLinks(List.of(uploadedImageLink))
                .createAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());

        // when
        NoticeInfo result = sut.createNotice(adminId, request, images);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("공지사항 제목");
        assertThat(result.imageLinks()).containsExactly(uploadedImageLink);
        then(s3Service).should(times(1))
            .saveAndReturnWithCdn(eq("admin-notice-images"), eq(image));
        then(adminNotificationService).should(times(1))
            .createAdminNotification(any(AdminNoticeCreateCommand.class));
    }

    @Test
    @DisplayName("복수 이미지로 공지사항 생성에 성공한다")
    void success_create_notice_with_multiple_images() {
        // given
        Long adminId = 1L;
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "공지사항 내용"
        );

        MockMultipartFile image1 = new MockMultipartFile(
            "images",
            "uuid-1",
            "image/jpeg",
            "image1 content".getBytes()
        );
        MockMultipartFile image2 = new MockMultipartFile(
            "images",
            "uuid-2",
            "image/jpeg",
            "image2 content".getBytes()
        );
        List<MultipartFile> images = List.of(image1, image2);

        String uploadedImageLink1 = "https://cdn.example.com/admin-notice-images/image1.jpg";
        String uploadedImageLink2 = "https://cdn.example.com/admin-notice-images/image2.jpg";

        given(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(image1)))
            .willReturn(uploadedImageLink1);
        given(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(image2)))
            .willReturn(uploadedImageLink2);
        given(htmlContentProcessor.changeToCdn(anyString(), any(Map.class)))
            .willReturn("변환된 내용");
        given(adminNotificationService.createAdminNotification(any(AdminNoticeCreateCommand.class)))
            .willReturn(NoticeInfo.builder()
                .id(1L)
                .title("공지사항 제목")
                .content("변환된 내용")
                .imageLinks(List.of(uploadedImageLink1, uploadedImageLink2))
                .createAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());

        // when
        NoticeInfo result = sut.createNotice(adminId, request, images);

        // then
        assertThat(result).isNotNull();
        assertThat(result.imageLinks())
            .containsExactly(uploadedImageLink1, uploadedImageLink2);
        then(s3Service).should(times(1))
            .saveAndReturnWithCdn(eq("admin-notice-images"), eq(image1));
        then(s3Service).should(times(1))
            .saveAndReturnWithCdn(eq("admin-notice-images"), eq(image2));
    }

    @Test
    @DisplayName("이미지 업로드 중 실패하면 이전까지 업로드된 이미지를 삭제한다")
    void fail_when_second_image_upload_fails_then_delete_first_image() {
        // given
        Long adminId = 1L;
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "공지사항 내용"
        );

        MockMultipartFile image1 = new MockMultipartFile(
            "images",
            "uuid-1",
            "image/jpeg",
            "image1 content".getBytes()
        );
        MockMultipartFile image2 = new MockMultipartFile(
            "images",
            "uuid-2",
            "image/jpeg",
            "image2 content".getBytes()
        );
        List<MultipartFile> images = List.of(image1, image2);

        String uploadedImageLink1 = "https://cdn.example.com/admin-notice-images/image1.jpg";

        given(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(image1)))
            .willReturn(uploadedImageLink1);
        given(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(image2)))
            .willThrow(new RuntimeException("S3 upload failed"));

        // when & then
        assertThatThrownBy(() -> sut.createNotice(adminId, request, images))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.ADMIN_NOTICE_CREATION_FAILED.getMessage());

        // 첫 번째 이미지는 삭제되어야 함
        then(s3Service).should(times(1))
            .deleteImagesCdn(List.of(uploadedImageLink1));
    }

    @Test
    @DisplayName("공지사항 생성 실패 시 업로드된 이미지를 삭제한다")
    void fail_create_notice_then_delete_uploaded_images() {
        // given
        Long adminId = 1L;
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "공지사항 내용"
        );

        MockMultipartFile image = new MockMultipartFile(
            "images",
            "uuid-1",
            "image/jpeg",
            "test image content".getBytes()
        );
        List<MultipartFile> profileImages = List.of(image);

        String uploadedImageLink = "https://cdn.example.com/admin-notice-images/image1.jpg";
        List<String> uploadedImageLinks = List.of(uploadedImageLink);

        given(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(image)))
            .willReturn(uploadedImageLink);
        given(htmlContentProcessor.changeToCdn(anyString(), any(Map.class)))
            .willReturn("변환된 내용");
        given(adminNotificationService.createAdminNotification(any(AdminNoticeCreateCommand.class)))
            .willThrow(new BbangleException(BbangleErrorCode.ADMIN_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> sut.createNotice(adminId, request, profileImages))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.ADMIN_NOTICE_CREATION_FAILED.getMessage());

        then(s3Service).should(times(1))
            .saveAndReturnWithCdn(eq("admin-notice-images"), eq(image));
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
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "",  // 빈 제목
            "공지사항 내용"
        );

        MockMultipartFile image = new MockMultipartFile(
            "images",
            "uuid-1",
            "image/jpeg",
            "test image content".getBytes()
        );
        List<MultipartFile> profileImages = List.of(image);

        String uploadedImageLink = "https://cdn.example.com/admin-notice-images/image1.jpg";
        List<String> uploadedImageLinks = List.of(uploadedImageLink);

        given(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(image)))
            .willReturn(uploadedImageLink);
        given(htmlContentProcessor.changeToCdn(anyString(), any(Map.class)))
            .willReturn("변환된 내용");
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
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "공지사항 내용"
        );

        MockMultipartFile image = new MockMultipartFile(
            "images",
            "uuid-1",
            "image/jpeg",
            "test image content".getBytes()
        );
        List<MultipartFile> profileImages = List.of(image);

        String uploadedImageLink = "https://cdn.example.com/admin-notice-images/image1.jpg";
        List<String> uploadedImageLinks = List.of(uploadedImageLink);

        given(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(image)))
            .willReturn(uploadedImageLink);
        given(htmlContentProcessor.changeToCdn(anyString(), any(Map.class)))
            .willReturn("변환된 내용");
        given(adminNotificationService.createAdminNotification(any(AdminNoticeCreateCommand.class)))
            .willThrow(new BbangleException(BbangleErrorCode.ADMIN_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> sut.createNotice(nonExistentAdminId, request, profileImages))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.ADMIN_NOTICE_CREATION_FAILED.getMessage());

        then(s3Service).should(times(1))
            .saveAndReturnWithCdn(eq("admin-notice-images"), eq(image));
        then(adminNotificationService).should(times(1))
            .createAdminNotification(any(AdminNoticeCreateCommand.class));
        then(s3Service).should(times(1))
            .deleteImagesCdn(eq(uploadedImageLinks));
    }

    @Test
    @DisplayName("이미지 없이 공지사항 제목과 본문만 수정에 성공한다")
    void success_update_notice_without_images() {
        // given
        Long adminId = 1L;
        Long noticeId = 1L;
        AdminNotificationUpdateRequest request = new AdminNotificationUpdateRequest(
            "수정된 제목",
            "수정된 내용"
        );
        List<MultipartFile> images = List.of();

        given(adminNotificationService.updateAdminNotification(any(AdminNoticeUpdateCommand.class)))
            .willReturn(NoticeInfo.builder()
                .id(noticeId)
                .title("수정된 제목")
                .content("수정된 내용")
                .imageLinks(List.of())
                .createAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());

        // when
        NoticeInfo result = sut.updateNotice(adminId, noticeId, request, images);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("수정된 제목");
        assertThat(result.content()).isEqualTo("수정된 내용");
        assertThat(result.imageLinks()).isEmpty();
        then(adminNotificationService).should(times(1))
            .updateAdminNotification(any(AdminNoticeUpdateCommand.class));
        then(s3Service).should(never())
            .saveAndReturnWithCdn(anyString(), any(MultipartFile.class));
    }

    @Test
    @DisplayName("새로운 이미지를 추가하여 공지사항 수정에 성공한다")
    void success_update_notice_with_new_images() {
        // given
        Long adminId = 1L;
        Long noticeId = 1L;
        AdminNotificationUpdateRequest request = new AdminNotificationUpdateRequest(
            "수정된 제목",
            "<div><img src=\"uuid-1\"></div>"
        );

        MockMultipartFile newImage = new MockMultipartFile(
            "images",
            "uuid-1",
            "image/jpeg",
            "new image content".getBytes()
        );
        List<MultipartFile> images = List.of(newImage);

        String uploadedImageLink = "https://cdn.example.com/admin-notice-images/new-image.jpg";
        List<String> existingImageLinks = List.of();
        List<String> wholeImageLinks = List.of(uploadedImageLink);

        given(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(newImage)))
            .willReturn(uploadedImageLink);
        given(htmlContentProcessor.changeToCdn(anyString(), any(Map.class)))
            .willReturn("<div><img src=\"" + uploadedImageLink + "\"></div>");
        given(htmlContentProcessor.extractImageSrc(anyString()))
            .willReturn(wholeImageLinks);
        given(adminNotificationService.getExistingImageLinks(noticeId))
            .willReturn(existingImageLinks);
        given(adminNotificationService.updateAdminNotification(any(AdminNoticeUpdateCommand.class)))
            .willReturn(NoticeInfo.builder()
                .id(noticeId)
                .title("수정된 제목")
                .content("<div><img src=\"" + uploadedImageLink + "\"></div>")
                .imageLinks(wholeImageLinks)
                .createAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());

        // when
        NoticeInfo result = sut.updateNotice(adminId, noticeId, request, images);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("수정된 제목");
        assertThat(result.imageLinks()).containsExactly(uploadedImageLink);
        then(s3Service).should(times(1))
            .saveAndReturnWithCdn(eq("admin-notice-images"), eq(newImage));
        then(adminNotificationService).should(times(1))
            .updateAdminNotification(any(AdminNoticeUpdateCommand.class));
    }

    @Test
    @DisplayName("이미지 없이 본문만 수정해도 기존 이미지를 조회하여 삭제 처리한다")
    void success_update_notice_without_images_checks_existing_images() {
        // given
        Long adminId = 1L;
        Long noticeId = 1L;
        AdminNotificationUpdateRequest request = new AdminNotificationUpdateRequest(
            "수정된 제목",
            "<div>이미지가 없는 내용</div>"
        );

        List<MultipartFile> images = List.of();
        List<String> existingImages = List.of("https://cdn.example.com/old-image.jpg");

        given(htmlContentProcessor.extractImageSrc(request.content()))
            .willReturn(List.of());
        given(adminNotificationService.getExistingImageLinks(noticeId))
            .willReturn(existingImages);
        given(adminNotificationService.updateAdminNotification(any(AdminNoticeUpdateCommand.class)))
            .willReturn(NoticeInfo.builder()
                .id(noticeId)
                .title("수정된 제목")
                .content("<div>이미지가 없는 내용</div>")
                .imageLinks(List.of())
                .createAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());

        // when
        NoticeInfo result = sut.updateNotice(adminId, noticeId, request, images);

        // then
        assertThat(result).isNotNull();
        assertThat(result.imageLinks()).isEmpty();
        then(adminNotificationService).should(times(1))
            .updateAdminNotification(any(AdminNoticeUpdateCommand.class));
        // 이미지가 없어도 기존 이미지 조회함
        then(adminNotificationService).should(times(1))
            .getExistingImageLinks(noticeId);
        // 기존 이미지가 삭제되어야 함
        then(s3Service).should(times(1))
            .deleteImagesCdn(existingImages);
    }

    @Test
    @DisplayName("기존 이미지 일부 유지하고 새 이미지 추가하여 수정에 성공한다")
    void success_update_notice_keeping_some_and_adding_new_images() {
        // given
        Long adminId = 1L;
        Long noticeId = 1L;
        String oldImageLink = "https://cdn.example.com/admin-notice-images/old-image.jpg";
        String newImageLink = "https://cdn.example.com/admin-notice-images/new-image.jpg";

        AdminNotificationUpdateRequest request = new AdminNotificationUpdateRequest(
            "수정된 제목",
            "<div><img src=\"old-url\"><img src=\"uuid-1\"></div>"
        );

        MockMultipartFile newImage = new MockMultipartFile(
            "images",
            "uuid-1",
            "image/jpeg",
            "new image content".getBytes()
        );
        List<MultipartFile> images = List.of(newImage);

        List<String> existingImageLinks = List.of(oldImageLink);
        List<String> wholeImageLinks = List.of(oldImageLink, newImageLink);

        given(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(newImage)))
            .willReturn(newImageLink);
        given(htmlContentProcessor.changeToCdn(anyString(), any(Map.class)))
            .willReturn("<div><img src=\"" + oldImageLink + "\"><img src=\"" + newImageLink + "\"></div>");
        given(htmlContentProcessor.extractImageSrc(anyString()))
            .willReturn(wholeImageLinks);
        given(adminNotificationService.getExistingImageLinks(noticeId))
            .willReturn(existingImageLinks);
        given(adminNotificationService.updateAdminNotification(any(AdminNoticeUpdateCommand.class)))
            .willReturn(NoticeInfo.builder()
                .id(noticeId)
                .title("수정된 제목")
                .content("<div><img src=\"" + oldImageLink + "\"><img src=\"" + newImageLink + "\"></div>")
                .imageLinks(wholeImageLinks)
                .createAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());

        // when
        NoticeInfo result = sut.updateNotice(adminId, noticeId, request, images);

        // then
        assertThat(result).isNotNull();
        assertThat(result.imageLinks()).containsExactly(oldImageLink, newImageLink);
        then(s3Service).should(times(1))
            .saveAndReturnWithCdn(eq("admin-notice-images"), eq(newImage));
    }

    @Test
    @DisplayName("이미지 업로드 실패 시 업로드된 이미지를 삭제하고 적절한 에러를 발생시킨다")
    void fail_update_notice_when_image_upload_fails_then_rollback() {
        // given
        Long adminId = 1L;
        Long noticeId = 1L;
        AdminNotificationUpdateRequest request = new AdminNotificationUpdateRequest(
            "수정된 제목",
            "<div><img src=\"uuid-1\"></div>"
        );

        MockMultipartFile newImage = new MockMultipartFile(
            "images",
            "uuid-1",
            "image/jpeg",
            "new image content".getBytes()
        );
        List<MultipartFile> images = List.of(newImage);

        given(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(newImage)))
            .willThrow(new RuntimeException("S3 upload failed"));

        // when & then
        assertThatThrownBy(() -> sut.updateNotice(adminId, noticeId, request, images))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.ADMIN_NOTICE_UPDATE_FAILED.getMessage());

        then(s3Service).should(never())
            .deleteImagesCdn(anyList());
        then(adminNotificationService).should(never())
            .updateAdminNotification(any(AdminNoticeUpdateCommand.class));
    }

    @Test
    @DisplayName("DB 업데이트 실패 시 업로드된 새 이미지를 삭제한다")
    void fail_update_notice_when_db_update_fails_then_delete_uploaded_images() {
        // given
        Long adminId = 1L;
        Long noticeId = 1L;
        AdminNotificationUpdateRequest request = new AdminNotificationUpdateRequest(
            "수정된 제목",
            "<div><img src=\"uuid-1\"></div>"
        );

        MockMultipartFile newImage = new MockMultipartFile(
            "images",
            "uuid-1",
            "image/jpeg",
            "new image content".getBytes()
        );
        List<MultipartFile> images = List.of(newImage);

        String uploadedImageLink = "https://cdn.example.com/admin-notice-images/new-image.jpg";
        List<String> wholeImageLinks = List.of(uploadedImageLink);

        given(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(newImage)))
            .willReturn(uploadedImageLink);
        given(htmlContentProcessor.changeToCdn(anyString(), any(Map.class)))
            .willReturn("<div><img src=\"" + uploadedImageLink + "\"></div>");
        given(htmlContentProcessor.extractImageSrc(anyString()))
            .willReturn(wholeImageLinks);
        given(adminNotificationService.getExistingImageLinks(noticeId))
            .willReturn(List.of());
        given(adminNotificationService.updateAdminNotification(any(AdminNoticeUpdateCommand.class)))
            .willThrow(new BbangleException(BbangleErrorCode.NOT_FIND_NOTICE));

        // when & then
        assertThatThrownBy(() -> sut.updateNotice(adminId, noticeId, request, images))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.ADMIN_NOTICE_UPDATE_FAILED.getMessage());

        then(s3Service).should(times(1))
            .deleteImagesCdn(List.of(uploadedImageLink));
    }

    @Test
    @DisplayName("기존 이미지 삭제와 새 이미지 추가를 동시에 수행하여 수정에 성공한다")
    void success_update_notice_removing_old_and_adding_new_images() {
        // given
        Long adminId = 1L;
        Long noticeId = 1L;
        String oldImageLink = "https://cdn.example.com/admin-notice-images/old-image.jpg";
        String newImageLink = "https://cdn.example.com/admin-notice-images/new-image.jpg";

        AdminNotificationUpdateRequest request = new AdminNotificationUpdateRequest(
            "수정된 제목",
            "<div><img src=\"uuid-1\"></div>"
        );

        MockMultipartFile newImage = new MockMultipartFile(
            "images",
            "uuid-1",
            "image/jpeg",
            "new image content".getBytes()
        );
        List<MultipartFile> images = List.of(newImage);

        List<String> existingImageLinks = List.of(oldImageLink);
        List<String> wholeImageLinks = List.of(newImageLink);

        given(s3Service.saveAndReturnWithCdn(eq("admin-notice-images"), eq(newImage)))
            .willReturn(newImageLink);
        given(htmlContentProcessor.changeToCdn(anyString(), any(Map.class)))
            .willReturn("<div><img src=\"" + newImageLink + "\"></div>");
        given(htmlContentProcessor.extractImageSrc(anyString()))
            .willReturn(wholeImageLinks);
        given(adminNotificationService.getExistingImageLinks(noticeId))
            .willReturn(existingImageLinks);
        given(adminNotificationService.updateAdminNotification(any(AdminNoticeUpdateCommand.class)))
            .willReturn(NoticeInfo.builder()
                .id(noticeId)
                .title("수정된 제목")
                .content("<div><img src=\"" + newImageLink + "\"></div>")
                .imageLinks(wholeImageLinks)
                .createAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());

        // when
        NoticeInfo result = sut.updateNotice(adminId, noticeId, request, images);

        // then
        assertThat(result).isNotNull();
        assertThat(result.imageLinks()).containsExactly(newImageLink);
        assertThat(result.imageLinks()).doesNotContain(oldImageLink);
        then(s3Service).should(times(1))
            .deleteImagesCdn(List.of(oldImageLink));
    }
}
