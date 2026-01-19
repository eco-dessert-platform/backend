package com.bbangle.bbangle.notification.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.admin.domain.Admin;
import com.bbangle.bbangle.admin.repository.AdminRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeCommand.AdminNoticeCreateCommand;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeCommand.AdminNoticeUpdateCommand;
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
import org.springframework.data.domain.PageRequest;
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

    @Test
    @DisplayName("이미지 포함하여 공지사항 수정에 성공한다")
    void success_updateAdminNotification_WithImages() throws JsonProcessingException {
        // given - 먼저 공지사항 생성
        String originalTitle = "원본 제목";
        String originalContent = "<div>원본 내용</div>";
        List<String> originalImageLinks = List.of("https://cdn.example.com/old-image.jpg");

        var createCommand = new AdminNoticeCreateCommand(
            testAdminId,
            originalTitle,
            originalContent,
            originalImageLinks
        );
        NoticeInfo createdNotice = sut.createAdminNotification(createCommand);
        em.flush();
        em.clear();

        // 수정할 데이터
        String updatedTitle = "수정된 제목";
        String updatedContent = "<div>수정된 내용</div>";
        List<String> updatedImageLinks = List.of(
            "https://cdn.example.com/new-image1.jpg",
            "https://cdn.example.com/new-image2.jpg"
        );

        var updateCommand = AdminNoticeUpdateCommand.builder()
            .adminId(testAdminId)
            .noticeId(createdNotice.id())
            .title(updatedTitle)
            .content(updatedContent)
            .cdnImageLinks(updatedImageLinks)
            .build();

        // when
        NoticeInfo result = sut.updateAdminNotification(updateCommand);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(createdNotice.id());
        assertThat(result.title()).isEqualTo(updatedTitle);
        assertThat(result.content()).isEqualTo(updatedContent);
        assertThat(result.imageLinks()).containsExactlyElementsOf(updatedImageLinks);

        // DB에서 직접 확인 (Dirty Checking 검증)
        em.flush();
        em.clear();
        Notice updatedNotice = notificationRepository.findById(createdNotice.id()).orElseThrow();
        assertThat(updatedNotice.getTitle()).isEqualTo(updatedTitle);
        assertThat(updatedNotice.getContent()).isEqualTo(updatedContent);

        List<String> savedImageLinks = objectMapper.readValue(
            updatedNotice.getImageLinks(),
            new TypeReference<List<String>>() {}
        );
        assertThat(savedImageLinks).containsExactlyElementsOf(updatedImageLinks);
    }

    @Test
    @DisplayName("이미지 없이 공지사항 수정에 성공한다")
    void success_updateAdminNotification_WithoutImages() {
        // given - 먼저 공지사항 생성
        String originalTitle = "원본 제목";
        String originalContent = "<div>원본 내용</div>";

        var createCommand = new AdminNoticeCreateCommand(
            testAdminId,
            originalTitle,
            originalContent,
            List.of()
        );
        NoticeInfo createdNotice = sut.createAdminNotification(createCommand);
        em.flush();
        em.clear();

        // 수정할 데이터
        String updatedTitle = "수정된 제목";
        String updatedContent = "<div>수정된 내용</div>";

        var updateCommand = AdminNoticeUpdateCommand.builder()
            .adminId(testAdminId)
            .noticeId(createdNotice.id())
            .title(updatedTitle)
            .content(updatedContent)
            .build();

        // when
        NoticeInfo result = sut.updateAdminNotification(updateCommand);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(createdNotice.id());
        assertThat(result.title()).isEqualTo(updatedTitle);
        assertThat(result.content()).isEqualTo(updatedContent);

        // DB에서 직접 확인 (Dirty Checking 검증)
        em.flush();
        em.clear();
        Notice updatedNotice = notificationRepository.findById(createdNotice.id()).orElseThrow();
        assertThat(updatedNotice.getTitle()).isEqualTo(updatedTitle);
        assertThat(updatedNotice.getContent()).isEqualTo(updatedContent);
    }

    @Test
    @DisplayName("JPA Dirty Checking을 통해 자동으로 DB에 업데이트된다")
    void success_updateAdminNotification_DirtyCheckingWorks() throws JsonProcessingException {
        // given - 먼저 공지사항 생성
        var createCommand = new AdminNoticeCreateCommand(
            testAdminId,
            "원본 제목",
            "<div>원본 내용</div>",
            List.of("https://cdn.example.com/image.jpg")
        );
        NoticeInfo createdNotice = sut.createAdminNotification(createCommand);
        Long noticeId = createdNotice.id();
        em.flush();
        em.clear();

        // when - 수정 실행 (트랜잭션 내에서)
        var updateCommand = AdminNoticeUpdateCommand.builder()
            .adminId(testAdminId)
            .noticeId(noticeId)
            .title("수정된 제목")
            .content("<div>수정된 내용</div>")
            .cdnImageLinks(List.of("https://cdn.example.com/new-image.jpg"))
            .build();

        NoticeInfo updatedResult = sut.updateAdminNotification(updateCommand);

        // 명시적으로 save()를 호출하지 않았지만 Dirty Checking으로 자동 업데이트
        em.flush();
        em.clear();

        // then - DB에서 재조회하여 변경사항이 반영되었는지 확인
        Notice savedNotice = notificationRepository.findById(noticeId).orElseThrow();
        assertThat(savedNotice.getTitle()).isEqualTo("수정된 제목");
        assertThat(savedNotice.getContent()).isEqualTo("<div>수정된 내용</div>");

        List<String> savedImageLinks = objectMapper.readValue(
            savedNotice.getImageLinks(),
            new TypeReference<List<String>>() {}
        );
        assertThat(savedImageLinks).containsExactly("https://cdn.example.com/new-image.jpg");
    }

    @Test
    @DisplayName("존재하지 않는 Admin으로 공지사항 수정 시 실패한다")
    void fail_updateAdminNotification_WithNonExistentAdmin() {
        // given - 먼저 공지사항 생성
        var createCommand = new AdminNoticeCreateCommand(
            testAdminId,
            "원본 제목",
            "<div>원본 내용</div>",
            List.of()
        );
        NoticeInfo createdNotice = sut.createAdminNotification(createCommand);
        em.flush();
        em.clear();

        // 존재하지 않는 Admin ID로 수정 시도
        Long invalidAdminId = 99999L;
        var updateCommand = AdminNoticeUpdateCommand.builder()
            .adminId(invalidAdminId)
            .noticeId(createdNotice.id())
            .title("수정된 제목")
            .content("<div>수정된 내용</div>")
            .build();

        // when & then
        assertThatThrownBy(() -> sut.updateAdminNotification(updateCommand))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.ADMIN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 Notice로 수정 시 실패한다")
    void fail_updateAdminNotification_WithNonExistentNotice() {
        // given
        Long invalidNoticeId = 99999L;
        var updateCommand = AdminNoticeUpdateCommand.builder()
            .adminId(testAdminId)
            .noticeId(invalidNoticeId)
            .title("수정된 제목")
            .content("<div>수정된 내용</div>")
            .build();

        // when & then
        assertThatThrownBy(() -> sut.updateAdminNotification(updateCommand))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.NOT_FIND_NOTICE.getMessage());
    }

    @Test
    @DisplayName("이미지 링크를 null에서 빈 리스트로 변경할 수 있다")
    void success_updateAdminNotification_ImageLinks_FromNullToEmpty() {
        // given - 먼저 이미지가 있는 공지사항 생성
        var createCommand = new AdminNoticeCreateCommand(
            testAdminId,
            "원본 제목",
            "<div>원본 내용</div>",
            List.of("https://cdn.example.com/image.jpg")
        );
        NoticeInfo createdNotice = sut.createAdminNotification(createCommand);
        em.flush();
        em.clear();

        // 이미지를 제거하는 수정 (cdnImageLinks를 null로 설정)
        var updateCommand = AdminNoticeUpdateCommand.builder()
            .adminId(testAdminId)
            .noticeId(createdNotice.id())
            .title("수정된 제목")
            .content("<div>이미지 없는 내용</div>")
            .cdnImageLinks(null)  // 이미지 제거
            .build();

        // when
        NoticeInfo result = sut.updateAdminNotification(updateCommand);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("수정된 제목");
        assertThat(result.content()).isEqualTo("<div>이미지 없는 내용</div>");

        // DB 확인
        em.flush();
        em.clear();
        Notice updatedNotice = notificationRepository.findById(createdNotice.id()).orElseThrow();
        assertThat(updatedNotice.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedNotice.getContent()).isEqualTo("<div>이미지 없는 내용</div>");
    }

    @Test
    @DisplayName("여러 필드를 동시에 수정할 수 있다")
    void success_updateAdminNotification_MultipleFieldsAtOnce() throws JsonProcessingException {
        // given - 먼저 공지사항 생성
        var createCommand = new AdminNoticeCreateCommand(
            testAdminId,
            "원본 제목",
            "<div>원본 내용</div>",
            List.of("https://cdn.example.com/old1.jpg", "https://cdn.example.com/old2.jpg")
        );
        NoticeInfo createdNotice = sut.createAdminNotification(createCommand);
        em.flush();
        em.clear();

        // 모든 필드 수정
        String newTitle = "완전히 새로운 제목";
        String newContent = "<div><h1>완전히 새로운 내용</h1></div>";
        List<String> newImageLinks = List.of(
            "https://cdn.example.com/new1.jpg",
            "https://cdn.example.com/new2.jpg",
            "https://cdn.example.com/new3.jpg"
        );

        var updateCommand = AdminNoticeUpdateCommand.builder()
            .adminId(testAdminId)
            .noticeId(createdNotice.id())
            .title(newTitle)
            .content(newContent)
            .cdnImageLinks(newImageLinks)
            .build();

        // when
        NoticeInfo result = sut.updateAdminNotification(updateCommand);

        // then - 모든 필드가 업데이트되었는지 확인
        assertThat(result.title()).isEqualTo(newTitle);
        assertThat(result.content()).isEqualTo(newContent);
        assertThat(result.imageLinks()).hasSize(3);
        assertThat(result.imageLinks()).containsExactlyElementsOf(newImageLinks);

        // DB 확인
        em.flush();
        em.clear();
        Notice updatedNotice = notificationRepository.findById(createdNotice.id()).orElseThrow();
        assertThat(updatedNotice.getTitle()).isEqualTo(newTitle);
        assertThat(updatedNotice.getContent()).isEqualTo(newContent);

        List<String> savedImageLinks = objectMapper.readValue(
            updatedNotice.getImageLinks(),
            new TypeReference<List<String>>() {}
        );
        assertThat(savedImageLinks).containsExactlyElementsOf(newImageLinks);
    }

    @Test
    @DisplayName("공지사항 조회에 성공한다")
    void success_searchNotice() {
        // given - 공지사항 생성
        String title = "검색 테스트 공지사항";
        String content = "<div>검색 테스트 내용</div>";
        List<String> imageLinks = List.of("https://cdn.example.com/test-image.jpg");

        var createCommand = new AdminNoticeCreateCommand(
            testAdminId,
            title,
            content,
            imageLinks
        );
        NoticeInfo createdNotice = sut.createAdminNotification(createCommand);
        Long noticeId = createdNotice.id();
        em.flush();
        em.clear();

        // when
        var pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        var result = sut.searchNotice(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(noticeId);
        assertThat(result.getContent().get(0).title()).isEqualTo(title);
        assertThat(result.getContent().get(0).content()).isEqualTo(content);
        // 조회 시에는 imageLinks가 본문에 포함되어있어 빈 리스트로 반환됨
        assertThat(result.getContent().get(0).imageLinks()).isEmpty();
    }

    @Test
    @DisplayName("공지사항 조회 시 페이징이 올바르게 작동한다")
    void success_searchNotice_WithPagination() {
        // given - 여러 공지사항 생성
        Long noticeId = null;
        for (int i = 0; i < 5; i++) {
            var createCommand = new AdminNoticeCreateCommand(
                testAdminId,
                "공지사항 " + i,
                "<div>내용 " + i + "</div>",
                List.of()
            );
            NoticeInfo created = sut.createAdminNotification(createCommand);
            if (i == 0) {
                noticeId = created.id();
            }
        }
        em.flush();
        em.clear();

        // when - 첫 번째 페이지 조회
        var pageRequest1 = PageRequest.of(0, 1);
        var result1 = sut.searchNotice(pageRequest1);

        // then
        assertThat(result1).isNotNull();
        assertThat(result1.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(result1.getContent()).hasSize(1);
        assertThat(result1.getContent().get(0).id()).isEqualTo(5L);
    }

}