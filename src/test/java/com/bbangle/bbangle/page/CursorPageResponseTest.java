package com.bbangle.bbangle.page;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class CursorPageResponseTest {
    private static final Long NO_NEXT_CURSOR = -1L;

    @Test
    @DisplayName("데이터 사이즈가 페이지 사이즈보다 클때 성공한다")
    void testIdExtraction_WithMoreDataThanPageSize() {
        // Given
        List<TestItem> items = Arrays.asList(
                new TestItem(1L),
                new TestItem(2L),
                new TestItem(3L),
                new TestItem(4L) // Extra item
        );
        int pageSize = 3;

        // When
        CursorPageResponse<TestItem> response = CursorPageResponse.of(
                items, pageSize, TestItem::getId
        );

        // Then
        assertEquals(3, response.getData().size());
        assertEquals(3L, response.getNextCursor());
        assertTrue(response.getHasNext());
    }

    @Test
    @DisplayName("데이터 사이즈가 페이지 사이즈보다 같을 때 hasNext는 false이다")
    void testIdExtraction_WithExactPageSize() {
        // Given
        List<TestItem> items = Arrays.asList(
                new TestItem(1L),
                new TestItem(2L),
                new TestItem(3L)
        );
        int pageSize = 3;

        // When
        CursorPageResponse<TestItem> response = CursorPageResponse.of(
                items, pageSize, TestItem::getId
        );

        // Then
        assertEquals(3, response.getData().size());
        assertEquals(NO_NEXT_CURSOR, response.getNextCursor());
        assertFalse(response.getHasNext());
    }

    @Test
    @DisplayName("데이터 사이즈가 페이지 사이즈보다 작을 때 hasNext는 false이다")
    void testIdExtraction_WithLessDataThanPageSize() {
        // Given
        List<TestItem> items = Arrays.asList(
                new TestItem(1L),
                new TestItem(2L)
        );
        int pageSize = 3;

        // When
        CursorPageResponse<TestItem> response = CursorPageResponse.of(
                items, pageSize, TestItem::getId
        );

        // Then
        assertEquals(2, response.getData().size());
        assertEquals(NO_NEXT_CURSOR, response.getNextCursor());
        assertFalse(response.getHasNext());
    }

    @Test
    @DisplayName("데이터 사이즈가 비었을 때 hasNext는 false이다")
    void testIdExtraction_WithEmptyList() {
        // Given
        List<TestItem> items = List.of();
        int pageSize = 3;

        // When
        CursorPageResponse<TestItem> response = CursorPageResponse.of(
                items, pageSize, TestItem::getId
        );

        // Then
        assertTrue(response.getData().isEmpty());
        assertEquals(NO_NEXT_CURSOR, response.getNextCursor());
        assertFalse(response.getHasNext());
    }

    // 테스트를 위한 내부 클래스
    private static class TestItem {
        private final Long id;

        TestItem(Long id) {
            this.id = id;
        }

        Long getId() {
            return id;
        }
    }
}