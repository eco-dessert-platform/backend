package com.bbangle.bbangle.board.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] BoardDetail 도메인")
class BoardDetailTest {

    @Nested
    @DisplayName("update 메서드")
    class Update {

        @Test
        @DisplayName("정상적인 입력으로 BoardDetail content를 수정한다")
        void success() {
            // given
            BoardDetail boardDetail = BoardDetail.builder()
                .content("<p>기존 상세 내용</p>")
                .build();

            // when
            boardDetail.update("<p>수정된 상세 내용</p><img src=\"https://cdn.bbangle.com/img.png\">");

            // then
            assertThat(boardDetail.getContent())
                .isEqualTo("<p>수정된 상세 내용</p><img src=\"https://cdn.bbangle.com/img.png\">");
        }

        @Test
        @DisplayName("빈 문자열로 content를 수정한다")
        void updateWithEmptyContent() {
            // given
            BoardDetail boardDetail = BoardDetail.builder()
                .content("<p>기존 내용</p>")
                .build();

            // when
            boardDetail.update("");

            // then
            assertThat(boardDetail.getContent()).isEmpty();
        }
    }
}
