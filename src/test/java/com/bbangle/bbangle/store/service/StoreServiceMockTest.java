package com.bbangle.bbangle.store.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.dto.BoardInfoDto;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.page.CursorPageResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class StoreServiceMockTest extends AbstractIntegrationTest {

    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private StoreService storeService;

    public BoardInfoDto createMockBoardInfoDto(Long id) {
        return new BoardInfoDto(
            id,                             // boardId
            "thumbnail_url" + id,           // boardProfile (썸네일 이미지 URL)
            "Delicious Bread" + id,         // boardTitle (상품 제목)
            15000,                          // boardPrice (상품 가격)
            10,                             // boardDiscount (할인율)
            BigDecimal.valueOf(4.5),        // boardReviewGrade (리뷰 평점)
            100L,                           // boardReviewCount (리뷰 수)
            0,                              // isSoldOut (품절 여부 0: false, 1: true)
            true,                           // isNotification (알림 여부)
            1,                              // glutenFreeTag (글루텐프리 태그 0: false, 1: true)
            0,                              // highProteinTag (고단백 태그)
            1,                              // sugarFreeTag (무설탕 태그)
            0,                              // veganTag (비건 태그)
            1,                              // ketogenicTag (케토제닉 태그)
            false,                          // isBundled (번들 상품 여부)
            true                            // isWished (위시리스트 여부)
        );
    }

    @Test
    void testGetBoardsInStoreWithCursor() {
        Long memberId = 123L;
        Long storeId = 456L;
        Long boardIdAsCursorId = 1L;

        List<BoardInfoDto> boardInfoDtos = new ArrayList<>();

        for (long i = 12; 0 < i; i--) {
            boardInfoDtos.add(createMockBoardInfoDto(i));
        }

        when(boardRepository.findBoardsByStoreWithCursor(storeId, memberId, boardIdAsCursorId))
            .thenReturn(boardInfoDtos);

        CursorPageResponse<BoardInfoDto> result = storeService.getBoardsInStore(memberId, storeId, boardIdAsCursorId);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotEmpty();
        assertThat(result.getData()).hasSize(10);

        assertThat(result.getHasNext()).isTrue();

        assertThat(result.getData().get(result.getData().size() - 1).getTitle()).isEqualTo("Delicious Bread3");

        verify(boardRepository, times(1)).findBoardsByStoreWithCursor(storeId, memberId, boardIdAsCursorId);
        verify(boardRepository, never()).findBoardsByStore(anyLong(), anyLong());
    }
}