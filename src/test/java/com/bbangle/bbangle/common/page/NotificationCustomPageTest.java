package com.bbangle.bbangle.common.page;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.notification.dto.NotificationResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[DTO] NotificationCustomPage")
class NotificationCustomPageTest {

    @DisplayName("responseList 크기가 pageSize 이하이면 hasNext는 false이고 전체 리스트가 반환된다.")
    @Test
    void givenListSizeLessOrEqualToPageSize_whenFrom_thenHasNextFalse() {
        // Given
        Long pageSize = 3L;
        List<NotificationResponse> responseList = List.of(
                new NotificationResponse(1L, "title1", "content1", "2023-10-01 12:00"),
                new NotificationResponse(2L, "title2", "content2", "2023-10-02 12:00")
        );

        // When
        NotificationCustomPage<List<NotificationResponse>> result =
                NotificationCustomPage.from(responseList, pageSize);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isEqualTo(2L);
    }

    @DisplayName("responseList 크기가 pageSize보다 크면 hasNext는 true이고 pageSize만큼만 잘린다.")
    @Test
    void givenListSizeGreaterThanPageSize_whenFrom_thenHasNextTrueAndLimitedList() {
        // Given
        Long pageSize = 2L;
        List<NotificationResponse> responseList = List.of(
                new NotificationResponse(1L, "title1", "content1", "2023-10-01 12:00"),
                new NotificationResponse(2L, "title2", "content2", "2023-10-02 12:00"),
                new NotificationResponse(3L, "title3", "content3", "2023-10-03 12:00")
        );

        // When
        NotificationCustomPage<List<NotificationResponse>> result =
                NotificationCustomPage.from(responseList, pageSize);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getNextCursor()).isEqualTo(3L);
    }

    @DisplayName("responseList가 비어있으면 requestCursor는 0L이고 hasNext는 false이다.")
    @Test
    void givenEmptyList_whenFrom_thenCursorIsZeroAndHasNextFalse() {
        // Given
        Long pageSize = 2L;

        // When
        NotificationCustomPage<List<NotificationResponse>> result =
                NotificationCustomPage.from(List.of(), pageSize);

        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getNextCursor()).isEqualTo(0L);
    }

}