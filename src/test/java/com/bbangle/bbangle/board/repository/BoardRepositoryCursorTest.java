package com.bbangle.bbangle.board.repository;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.CursorInfo;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.page.BoardCustomPage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BoardRepositoryCursorTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("[게시글조회] 추천순 점수 내림차순 정렬 & 커서 정상 확인")
    void test1() {
        // given
        List<Long> idList = new ArrayList<>();
        for (int score = 0; score < 5; score++) {
            Board board = fixtureBoard(emptyMap());
            idList.add(board.getId());

            fixtureRanking(Map.of("board", board, "recommendScore", (double) score));
        }

        FilterRequest filter = FilterRequest.builder()
            .build();
        Long cursorId = idList.get(3); // 아마도 3
        Double cursorScore = 3.0;
        CursorInfo cursor = new CursorInfo(cursorId, cursorScore);

        // when
        BoardCustomPage<List<BoardResponseDto>> resultPage = boardRepository
            .getBoardResponseList(filter, SortType.RECOMMEND, cursor);
        List<BoardResponseDto> result = resultPage.getContent();

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getBoardId()).isEqualTo(idList.get(2));
        assertThat(result.get(1).getBoardId()).isEqualTo(idList.get(1));
        assertThat(result.get(2).getBoardId()).isEqualTo(idList.get(0));
    }


    @Test
    @DisplayName("[게시글조회] 인기순 점수 내림차순 정렬 & 커서 정상 확인")
    void test2() {
        // given
        List<Long> idList = new ArrayList<>();
        for (int score = 0; score < 5; score++) {
            Board board = fixtureBoard(emptyMap());
            idList.add(board.getId());

            fixtureRanking(Map.of("board", board, "popularScore", (double) score));
        }

        FilterRequest filter = FilterRequest.builder()
            .build();
        Long cursorId = idList.get(3); // 아마도 3
        Double cursorScore = 3.0;
        CursorInfo cursor = new CursorInfo(cursorId, cursorScore);

        // when
        BoardCustomPage<List<BoardResponseDto>> resultPage = boardRepository
            .getBoardResponseList(filter, SortType.POPULAR, cursor);
        List<BoardResponseDto> result = resultPage.getContent();

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getBoardId()).isEqualTo(idList.get(2));
        assertThat(result.get(1).getBoardId()).isEqualTo(idList.get(1));
        assertThat(result.get(2).getBoardId()).isEqualTo(idList.get(0));
    }
}
