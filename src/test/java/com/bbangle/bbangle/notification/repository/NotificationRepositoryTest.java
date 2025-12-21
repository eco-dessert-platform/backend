package com.bbangle.bbangle.notification.repository;

import static com.bbangle.bbangle.exception.BbangleErrorCode.NOTIFICATION_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.common.page.NotificationCustomPage;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.NoticeFixture;
import com.bbangle.bbangle.notification.customer.dto.NotificationResponse;
import com.bbangle.bbangle.notification.domain.Notice;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * JpaRepository의 기본 메서드는 테스트 생략
 */
@DisplayName("[Repository] - NotificationRepository")
@ActiveProfiles("test")
@Import({
    TestContainersConfig.class,
    QueryDslConfig.class,
    SearchFilter.class,
    SearchSort.class
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository sut;

    @Autowired
    private EntityManager em;

    private static final Long PAGE_SIZE = 20L;

    /**
     * 테스트 전 auto increment 초기화
     */
    @BeforeEach
    void resetTable() {
        em.flush(); // 대기 중인 SQL 먼저 반영
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE notice").executeUpdate(); // AUTO_INCREMENT = 1로 재설정
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }

    @DisplayName("cursorId가 null일 때, 첫 페이지 정상 조회")
    @Test
    void givenCursorIdIsNull_whenFindNextCursorPage_thenReturnsFirstPage() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        List<Notice> notices = IntStream.rangeClosed(1, 10)
            .mapToObj(i -> NoticeFixture.notice("title" + i, "content" + i, now.minusDays(i)))
            .toList();
        sut.saveAll(notices);

        // When
        NotificationCustomPage<List<NotificationResponse>> result = sut.findNextCursorPage(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNotNull();
    }


    @DisplayName("유효한 cursorId가 주어질 때, 다음 페이지 정상 조회")
    @Test
    void givenValidCursorId_whenFindNextCursorPage_thenReturnsNextPage() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        List<Notice> notices = IntStream.rangeClosed(1, 25)
            .mapToObj(i -> NoticeFixture.notice("title" + i, "content" + i, now.minusDays(i)))
            .toList();
        sut.saveAll(notices);

        // cursorId를 21로 가정 (21 ~ 2 조회)
        Long cursorId = notices.get(20).getId();
        assertThat(cursorId).isEqualTo(21L);

        // When
        NotificationCustomPage<List<NotificationResponse>> result = sut.findNextCursorPage(
            cursorId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent()).hasSize(PAGE_SIZE.intValue());
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
    }

    @DisplayName("유효한 cursorId가 주어질 때, 다음 페이지 데이터가 PAGE_SIZE 미만이면 hasNext가 false 반환")
    @Test
    void givenValidCursorId_whenFindNextCursorPageWithLessThanPageSize_thenReturnsLastPage() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        List<Notice> notices = IntStream.rangeClosed(1, 25)
            .mapToObj(i -> NoticeFixture.notice("title" + i, "content" + i, now.minusDays(i)))
            .toList();
        sut.saveAll(notices);

        // cursorId를 6으로 가정 (6 ~ 1 조회)
        Long cursorId = notices.get(5).getId();
        assertThat(cursorId).isEqualTo(6L);

        // When
        NotificationCustomPage<List<NotificationResponse>> result = sut.findNextCursorPage(
            cursorId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().size()).isLessThan(PAGE_SIZE.intValue());
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNotNull();
    }

    @DisplayName("존재하지 않는 cursorId가 주어질 때, 예외 발생")
    @Test
    void givenInvalidCursorId_whenFindNextCursorPage_thenThrowsBbangleException() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        sut.saveAll(List.of(
            NoticeFixture.notice("title1", "content1", now.minusDays(3)),
            NoticeFixture.notice("title2", "content2", now.minusDays(2))
        ));

        Long invalidCursorId = 99999L;

        // When
        BbangleException exception = assertThrows(BbangleException.class,
            () -> sut.findNextCursorPage(invalidCursorId));

        // Then
        assertThat(exception).isInstanceOf(BbangleException.class);
        assertThat(exception.getBbangleErrorCode()).isEqualTo(NOTIFICATION_NOT_FOUND);
    }

    @DisplayName("데이터가 없을 때, 빈 결과 반환")
    @Test
    void givenEmptyDatabase_whenFindNextCursorPage_thenReturnsEmptyResult() {
        // Given 데이터 없음

        // When
        NotificationCustomPage<List<NotificationResponse>> result = sut.findNextCursorPage(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isEqualTo(0L);
    }

    @DisplayName("데이터가 페이지 사이즈보다 클 때, hasNext가 true 반환")
    @Test
    void givenDataMoreThanPageSize_whenFindNextCursorPage_thenReturnsHasNextTrue() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        List<Notice> notices = IntStream.rangeClosed(1, 25)
            .mapToObj(i -> NoticeFixture.notice("title" + i, "content" + i, now.minusDays(i)))
            .toList();
        sut.saveAll(notices);

        // When
        NotificationCustomPage<List<NotificationResponse>> result = sut.findNextCursorPage(null);

        // Then
        assertThat(result.getContent()).hasSize(PAGE_SIZE.intValue());
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isNotNull();
    }

}

