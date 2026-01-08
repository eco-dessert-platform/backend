package com.bbangle.bbangle.board.admin.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.bbangle.bbangle.board.admin.controller.dto.AdminProductResponse;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.fixture.BoardFixture;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DisplayName("[단위 테스트] AdminBoardService")
@ExtendWith(MockitoExtension.class)
class AdminBoardServiceUnitTest {

    @InjectMocks
    private AdminBoardService sut;

    @Mock
    private BoardRepository boardRepository;

    @Test
    @DisplayName("관리자 상품 목록을 페이징 형태로 조회한다")
    void getAdminBoards_success() {
        // given
        int page = 0;
        int size = 20;
        Pageable pageable = PageRequest.of(page, size);
        Board board = BoardFixture.defaultBoard();
        Page<Board> boardPage = new PageImpl<>(List.of(board), pageable, 1);

        given(boardRepository.findByIsCrawlingTrueAndIsDeletedFalse(pageable)).willReturn(boardPage);

        // when
        Page<AdminProductResponse> result = sut.getAdminBoards(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        AdminProductResponse response = result.getContent().get(0);
        assertThat(response.productId()).isEqualTo(board.getId());
        assertThat(response.productName()).isEqualTo(board.getTitle());
        assertThat(response.productPrice()).isEqualTo(board.getPrice());

        assertThat(result.getNumber()).isEqualTo(page);
        assertThat(result.getSize()).isEqualTo(size);
        assertThat(result.getTotalElements()).isEqualTo(1);
        then(boardRepository).should().findByIsCrawlingTrueAndIsDeletedFalse(pageable);
    }

    @Test
    @DisplayName("조회 결과가 없으면 빈 Page를 반환한다")
    void getAdminBoards_emptyResult() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Board> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        given(boardRepository.findByIsCrawlingTrueAndIsDeletedFalse(pageable)).willReturn(emptyPage);

        // when
        Page<AdminProductResponse> result = sut.getAdminBoards(pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        then(boardRepository).should().findByIsCrawlingTrueAndIsDeletedFalse(pageable);
    }

}