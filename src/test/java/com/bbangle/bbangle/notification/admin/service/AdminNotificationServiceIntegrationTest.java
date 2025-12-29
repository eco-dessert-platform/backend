package com.bbangle.bbangle.notification.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.admin.domain.Admin;
import com.bbangle.bbangle.admin.repository.AdminRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.notification.admin.controller.dto.LinkDto;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationRequest.AdminNotificationCreateRequest;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeInfo.NoticeInfo;
import com.bbangle.bbangle.notification.domain.Notice;
import com.bbangle.bbangle.notification.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합 테스트] AdminNotificationService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminNotificationServiceIntegrationTest {

    @Autowired
    private AdminNotificationService sut;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("관리자 공지사항 생성에 성공한다 - links와 imageLinks 모두 포함")
    void success_create_admin_notification_with_links_and_imageLinks() throws JsonProcessingException {
        // arrange
        List<LinkDto> links = List.of(
            new LinkDto("https://example.com", "더보기"),
            new LinkDto("https://example2.com", "상세보기")
        );
        List<String> imageLinks = List.of(
            "https://image1.example.com",
            "https://image2.example.com"
        );
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "공지사항 내용",
            links
        );

        // act
        NoticeInfo result = sut.createAdminNotification(request.toCreateCommand(testAdminId, imageLinks));

        // assert
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("공지사항 제목");
        assertThat(result.content()).isEqualTo("공지사항 내용");

        // DB에 저장된 데이터 검증
        List<Notice> savedNotices = notificationRepository.findAll();
        assertThat(savedNotices).hasSize(1);

        Notice savedNotice = savedNotices.get(0);
        assertThat(savedNotice.getTitle()).isEqualTo("공지사항 제목");
        assertThat(savedNotice.getContent()).isEqualTo("공지사항 내용");

        // JSON 문자열로 저장된 links 파싱하여 검증
        List<LinkDto> savedLinks = objectMapper.readValue(
            savedNotice.getLinks(),
            new TypeReference<List<LinkDto>>() {
            }
        );
        assertThat(savedLinks).hasSize(2);
        assertThat(savedLinks.get(0).url()).isEqualTo("https://example.com");
        assertThat(savedLinks.get(0).linkText()).isEqualTo("더보기");

        // JSON 문자열로 저장된 imageLinks 파싱하여 검증
        List<String> savedImageLinks = objectMapper.readValue(
            savedNotice.getImageLinks(),
            new TypeReference<List<String>>() {
            }
        );
        assertThat(savedImageLinks).hasSize(2);
        assertThat(savedImageLinks).contains("https://image1.example.com", "https://image2.example.com");
    }

    @Test
    @DisplayName("links가 빈 리스트여도 공지사항 생성에 성공한다")
    void success_create_admin_notification_with_empty_links() throws JsonProcessingException {
        // arrange
        List<LinkDto> links = List.of();
        List<String> imageLinks = List.of("https://image.example.com");
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "공지사항 내용",
            links
        );

        // act
        NoticeInfo result = sut.createAdminNotification(request.toCreateCommand(testAdminId, imageLinks));

        // assert
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("공지사항 제목");
        assertThat(result.content()).isEqualTo("공지사항 내용");

        // DB 검증
        List<Notice> savedNotices = notificationRepository.findAll();
        assertThat(savedNotices).hasSize(1);

        Notice savedNotice = savedNotices.get(0);
        List<LinkDto> savedLinks = objectMapper.readValue(
            savedNotice.getLinks(),
            new TypeReference<List<LinkDto>>() {
            }
        );
        assertThat(savedLinks).isEmpty();
    }

    @Test
    @DisplayName("imageLinks가 빈 리스트여도 공지사항 생성에 성공한다")
    void success_create_admin_notification_with_empty_imageLinks() throws JsonProcessingException {
        // arrange
        List<LinkDto> links = List.of(new LinkDto("https://example.com", "더보기"));
        List<String> imageLinks = List.of();
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "공지사항 내용",
            links
        );

        // act
        NoticeInfo result = sut.createAdminNotification(request.toCreateCommand(testAdminId, imageLinks));

        // assert
        assertThat(result).isNotNull();

        // DB 검증
        List<Notice> savedNotices = notificationRepository.findAll();
        assertThat(savedNotices).hasSize(1);

        Notice savedNotice = savedNotices.get(0);
        List<String> savedImageLinks = objectMapper.readValue(
            savedNotice.getImageLinks(),
            new TypeReference<List<String>>() {
            }
        );
        assertThat(savedImageLinks).isEmpty();
    }

    @Test
    @DisplayName("title이 null이면 공지사항 생성에 실패한다")
    void fail_create_admin_notification_when_title_is_null() {
        // arrange
        List<LinkDto> links = List.of(new LinkDto("https://example.com", "더보기"));
        List<String> imageLinks = List.of("https://image.example.com");
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            null,
            "공지사항 내용",
            links
        );

        // act & assert
        assertThatThrownBy(() -> sut.createAdminNotification(request.toCreateCommand(testAdminId, imageLinks)))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.TITLE_IS_EMPTY.getMessage());

        // DB에 저장되지 않았는지 검증
        List<Notice> savedNotices = notificationRepository.findAll();
        assertThat(savedNotices).isEmpty();
    }

    @Test
    @DisplayName("title이 empty이면 공지사항 생성에 실패한다")
    void fail_create_admin_notification_when_title_is_empty() {
        // arrange
        List<LinkDto> links = List.of(new LinkDto("https://example.com", "더보기"));
        List<String> imageLinks = List.of("https://image.example.com");
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "",
            "공지사항 내용",
            links
        );

        // act & assert
        assertThatThrownBy(() -> sut.createAdminNotification(request.toCreateCommand(testAdminId, imageLinks)))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.TITLE_IS_EMPTY.getMessage());

        // DB에 저장되지 않았는지 검증
        List<Notice> savedNotices = notificationRepository.findAll();
        assertThat(savedNotices).isEmpty();
    }

    @Test
    @DisplayName("content가 null이면 공지사항 생성에 실패한다")
    void fail_create_admin_notification_when_content_is_null() {
        // arrange
        List<LinkDto> links = List.of(new LinkDto("https://example.com", "더보기"));
        List<String> imageLinks = List.of("https://image.example.com");
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            null,
            links
        );

        // act & assert
        assertThatThrownBy(() -> sut.createAdminNotification(request.toCreateCommand(testAdminId, imageLinks)))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.CONTENT_IS_EMPTY.getMessage());

        // DB에 저장되지 않았는지 검증
        List<Notice> savedNotices = notificationRepository.findAll();
        assertThat(savedNotices).isEmpty();
    }

    @Test
    @DisplayName("content가 empty이면 공지사항 생성에 실패한다")
    void fail_create_admin_notification_when_content_is_empty() {
        // arrange
        List<LinkDto> links = List.of(new LinkDto("https://example.com", "더보기"));
        List<String> imageLinks = List.of("https://image.example.com");
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "",
            links
        );

        // act & assert
        assertThatThrownBy(() -> sut.createAdminNotification(request.toCreateCommand(testAdminId, imageLinks)))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.CONTENT_IS_EMPTY.getMessage());

        // DB에 저장되지 않았는지 검증
        List<Notice> savedNotices = notificationRepository.findAll();
        assertThat(savedNotices).isEmpty();
    }

    @Test
    @DisplayName("생성된 공지사항은 createdAt과 modifiedAt이 자동으로 설정된다")
    void success_create_admin_notification_with_auto_timestamps() throws JsonProcessingException {
        // arrange
        List<LinkDto> links = List.of(new LinkDto("https://example.com", "더보기"));
        List<String> imageLinks = List.of("https://image.example.com");
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "공지사항 내용",
            links
        );

        // act
        NoticeInfo result = sut.createAdminNotification(request.toCreateCommand(testAdminId, imageLinks));

        // assert
        assertThat(result.createAt()).isNotNull();
        assertThat(result.modifiedAt()).isNotNull();

        // DB 검증
        List<Notice> savedNotices = notificationRepository.findAll();
        assertThat(savedNotices).hasSize(1);

        Notice savedNotice = savedNotices.get(0);
        assertThat(savedNotice.getCreatedAt()).isNotNull();
        assertThat(savedNotice.getModifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 Admin ID로 공지사항 생성 시 예외가 발생한다")
    void fail_create_admin_notification_when_admin_not_found() {
        // arrange
        Long nonExistentAdminId = 999L;
        List<LinkDto> links = List.of(new LinkDto("https://example.com", "더보기"));
        List<String> imageLinks = List.of("https://image.example.com");
        AdminNotificationCreateRequest request = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "공지사항 내용",
            links
        );

        // act & assert
        assertThatThrownBy(() -> sut.createAdminNotification(request.toCreateCommand(nonExistentAdminId, imageLinks)))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.ADMIN_NOT_FOUND.getMessage());

        // DB에 저장되지 않았는지 검증
        List<Notice> savedNotices = notificationRepository.findAll();
        assertThat(savedNotices).isEmpty();
    }
}