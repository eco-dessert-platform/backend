package com.bbangle.bbangle.board.admin.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.bbangle.bbangle.board.admin.controller.dto.AdminProductResponse;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] AdminBoardService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminBoardServiceIntegrationTest {

    @Autowired
    private AdminBoardService adminBoardService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    @DisplayName("상품이 존재하면 관리자 상품 목록을 페이징 형태로 조회한다")
    void getAdminBoards_success() {
        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());
        Board board1 = BoardFixture.defaultBoard(store, "상품1");
        Board board2 = BoardFixture.defaultBoard(store, "상품2");

        boardRepository.saveAndFlush(board1);
        boardRepository.saveAndFlush(board2);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<AdminProductResponse> result =
            adminBoardService.getAdminBoards(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);

        AdminProductResponse response = result.getContent().get(0);
        assertThat(response.productId()).isNotNull();
        assertThat(response.productName()).isNotBlank();
    }

    @Test
    @DisplayName("page가 범위를 초과하면 빈 목록을 반환한다")
    void getAdminBoards_outOfRangePage_returnsEmpty() {
        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());
        Board board = BoardFixture.defaultBoard(store, "상품1");
        boardRepository.saveAndFlush(board);

        Pageable pageable = PageRequest.of(10, 10); // 범위 초과

        // when
        Page<AdminProductResponse> result =
            adminBoardService.getAdminBoards(pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("상품이 하나도 없으면 빈 페이지를 반환한다")
    void getAdminBoards_emptyDatabase() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<AdminProductResponse> result = adminBoardService.getAdminBoards(pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
    }


}