package com.bbangle.bbangle.notification.customer.service;

import static com.bbangle.bbangle.exception.BbangleErrorCode.NOTIFICATION_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.common.page.NotificationCustomPage;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.NoticeFixture;
import com.bbangle.bbangle.notification.customer.dto.NotificationResponse;
import com.bbangle.bbangle.notification.domain.Notice;
import com.bbangle.bbangle.notification.repository.NotificationRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] NotificationService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationServiceIntegrationTest {

    @Autowired
    private NotificationService sut;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EntityManager em;

    private static final long PAGE_SIZE = 20L;

    @BeforeEach
    void resetTable() {
        em.flush();
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE notice").executeUpdate();
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }

    @DisplayName("유효한 cursorId 를 제공하면, 그 커서 기준 다음 페이지를 반환한다")
    @Test
    void givenValidCursorId_whenGetList_thenReturnNextPage() {
        // Given
        // 25개의 공지사항 생성
        LocalDateTime now = LocalDateTime.now();
        List<Notice> notices = IntStream.rangeClosed(1, 25)
            .mapToObj(i -> NoticeFixture.notice("title" + i, "content" + i, now.minusDays(i)))
            .toList();
        notificationRepository.saveAll(notices);

        Long cursorId = notices.get(20).getId(); // 21번째 항목의 id
        assertThat(cursorId).isNotNull();

        // When
        NotificationCustomPage<List<NotificationResponse>> page = sut.getList(cursorId);

        // Then
        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize((int) PAGE_SIZE);
        assertThat(page.getHasNext()).isTrue();
        assertThat(page.getNextCursor()).isNotNull();

        // 모든 항목의 id 가 cursorId 이하인지 확인 (정렬은 createdAt desc, id desc)
        assertThat(page.getContent())
            .allSatisfy(r -> assertThat(r.id()).isLessThanOrEqualTo(cursorId));
    }

    @DisplayName("존재하지 않는 cursorId 를 제공하면 예외가 발생한다")
    @Test
    void givenInvalidCursorId_whenGetList_thenThrowsBbangleException() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        notificationRepository.saveAll(List.of(
            NoticeFixture.notice("title1", "content1", now.minusDays(3)),
            NoticeFixture.notice("title2", "content2", now.minusDays(2))
        ));

        Long invalidCursorId = 99999L;

        // When
        assertThatThrownBy((()-> sut.getList(invalidCursorId)))
            .isInstanceOf(BbangleException.class)
            .hasMessage(NOTIFICATION_NOT_FOUND.getMessage());

    }
}
