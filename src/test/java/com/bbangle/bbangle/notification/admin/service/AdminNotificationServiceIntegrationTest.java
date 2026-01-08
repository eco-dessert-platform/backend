package com.bbangle.bbangle.notification.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.admin.domain.Admin;
import com.bbangle.bbangle.admin.repository.AdminRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
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
    @DisplayName("공지사항 생성에 성공한다")
    void success_createAdminNotification() {
        // given
        Admin admin = adminRepository.findById(testAdminId).orElseThrow();
        String title = "공지사항 제목";
        String content = "<div>공지사항 본문</div>";
        List<String> imageLinks = List.of(
            "https://cdn.example.com/image1.jpg",
            "https://cdn.example.com/image2.jpg"
        );

        var command = new com.bbangle.bbangle.notification.admin.service.model.AdminNoticeCommand.AdminNoticeCreateCommand(
            testAdminId,
            title,
            content,
            imageLinks
        );

        // when
        NoticeInfo result = sut.createAdminNotification(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.title()).isEqualTo(title);
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.imageLinks()).containsExactlyElementsOf(imageLinks);
    }

    @Test
    @DisplayName("공지사항 생성 후 DB에 저장되고 트랜잭션이 커밋된다")
    void success_createAdminNotification_SavesToDB() throws JsonProcessingException {
        // given
        String title = "공지사항 제목";
        String content = "<div>공지사항 본문</div>";
        List<String> imageLinks = List.of("https://cdn.example.com/image1.jpg");

        var command = new com.bbangle.bbangle.notification.admin.service.model.AdminNoticeCommand.AdminNoticeCreateCommand(
            testAdminId,
            title,
            content,
            imageLinks
        );

        // when
        NoticeInfo result = sut.createAdminNotification(command);
        em.flush();
        em.clear();

        // then - DB에서 조회
        Notice savedNotice = notificationRepository.findById(result.id()).orElseThrow();
        assertThat(savedNotice.getTitle()).isEqualTo(title);
        assertThat(savedNotice.getContent()).isEqualTo(content);

        // imageLinks JSON 역직렬화 검증
        List<String> savedImageLinks = objectMapper.readValue(
            savedNotice.getImageLinks(),
            new TypeReference<List<String>>() {}
        );
        assertThat(savedImageLinks).containsExactlyElementsOf(imageLinks);
    }

    @Test
    @DisplayName("공지사항 생성 시 Admin과의 관계가 설정된다")
    void success_createAdminNotification_SetsAdminRelation() {
        // given
        String title = "공지사항 제목";
        String content = "<div>공지사항 본문</div>";
        List<String> imageLinks = List.of("https://cdn.example.com/image1.jpg");

        var command = new com.bbangle.bbangle.notification.admin.service.model.AdminNoticeCommand.AdminNoticeCreateCommand(
            testAdminId,
            title,
            content,
            imageLinks
        );

        // when
        NoticeInfo result = sut.createAdminNotification(command);
        em.flush();
        em.clear();

        // then
        Notice savedNotice = notificationRepository.findById(result.id()).orElseThrow();
        assertThat(savedNotice.getAdmin()).isNotNull();
        assertThat(savedNotice.getAdmin().getId()).isEqualTo(testAdminId);
    }

    @Test
    @DisplayName("존재하지 않는 Admin으로 공지사항 생성 시 실패한다")
    void fail_createAdminNotification_WithNonExistentAdmin() {
        // given
        Long invalidAdminId = 99999L;
        String title = "공지사항 제목";
        String content = "<div>공지사항 본문</div>";
        List<String> imageLinks = List.of("https://cdn.example.com/image1.jpg");

        var command = new com.bbangle.bbangle.notification.admin.service.model.AdminNoticeCommand.AdminNoticeCreateCommand(
            invalidAdminId,
            title,
            content,
            imageLinks
        );

        // when & then
        assertThatThrownBy(() -> sut.createAdminNotification(command))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.ADMIN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("제목이 비어있으면 공지사항 생성에 실패한다")
    void fail_createAdminNotification_WithEmptyTitle() {
        // given
        String title = "";
        String content = "<div>공지사항 본문</div>";
        List<String> imageLinks = List.of("https://cdn.example.com/image1.jpg");

        var command = new com.bbangle.bbangle.notification.admin.service.model.AdminNoticeCommand.AdminNoticeCreateCommand(
            testAdminId,
            title,
            content,
            imageLinks
        );

        // when & then
        assertThatThrownBy(() -> sut.createAdminNotification(command))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.TITLE_IS_EMPTY.getMessage());
    }

    @Test
    @DisplayName("본문이 비어있으면 공지사항 생성에 실패한다")
    void fail_createAdminNotification_WithEmptyContent() {
        // given
        String title = "공지사항 제목";
        String content = "";
        List<String> imageLinks = List.of("https://cdn.example.com/image1.jpg");

        var command = new com.bbangle.bbangle.notification.admin.service.model.AdminNoticeCommand.AdminNoticeCreateCommand(
            testAdminId,
            title,
            content,
            imageLinks
        );

        // when & then
        assertThatThrownBy(() -> sut.createAdminNotification(command))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.CONTENT_IS_EMPTY.getMessage());
    }

}