package com.bbangle.bbangle.board.admin.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.bbangle.bbangle.board.admin.controller.dto.AdminProductResponse;
import com.bbangle.bbangle.board.admin.controller.dto.UploadApprovalResponse;
import com.bbangle.bbangle.board.admin.service.dto.RemoveProductsCommand;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import java.util.List;
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
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    @DisplayName("관리자 상품 목록은 크롤링되고 삭제되지 않은 상품만 조회된다")
    void getAdminBoards_filtersByCrawlingAndDeleted() {
        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());

        Board valid1 = BoardFixture.crawlingActiveBoardWithStore(store, "정상상품1");
        Board valid2 = BoardFixture.crawlingActiveBoardWithStore(store, "정상상품2");
        Board deleted = BoardFixture.crawlingDeletedBoardWithStore(store, "삭제상품");
        Board nonCrawling = BoardFixture.defaultBoardWithStore(store, "비크롤링상품");

        boardRepository.saveAll(List.of(valid1, valid2, deleted, nonCrawling));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<AdminProductResponse> result =
            adminBoardService.getAdminBoards(pageable);

        // then
        assertThat(result.getContent())
            .hasSize(2)
            .extracting(AdminProductResponse::productName)
            .containsExactlyInAnyOrder("정상상품1", "정상상품2");

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("페이지 범위를 초과하면 content는 비어 있고 total 정보는 유지된다")
    void getAdminBoards_outOfRangePage_returnsEmptyContent() {
        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());

        boardRepository.save(
            BoardFixture.crawlingActiveBoardWithStore(store, "정상상품")
        );

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
    @DisplayName("조회 대상이 되는 상품이 하나도 없으면 빈 페이지를 반환한다")
    void getAdminBoards_noValidBoards_returnsEmptyPage() {
        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());

        boardRepository.saveAll(
            List.of(
                BoardFixture.crawlingDeletedBoardWithStore(store, "삭제상품"),
                BoardFixture.nonCrawlingActiveBoardWithStore(store, "비크롤링상품"),
                BoardFixture.nonCrawlingDeletedBoardWithStore(store, "완전제외상품")
            )
        );

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<AdminProductResponse> result =
            adminBoardService.getAdminBoards(pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
    }

    @Test
    @DisplayName("관리자 상품 삭제 시 Board 및 연관 엔티티가 모두 soft delete 된다")
    void deleteBoards_softDeleteAllRelatedEntities() {
        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());
        Board board1 = BoardFixture.crawlingActiveBoardWithStore(store, "상품1");
        Board board2 = BoardFixture.crawlingActiveBoardWithStore(store, "상품2");

        boardRepository.saveAll(List.of(board1, board2));

        List<Long> boardIds = List.of(board1.getId(), board2.getId());

        // when
        adminBoardService.deleteBoards(boardIds);
        boardRepository.flush();

        // then
        List<Board> boards = boardRepository.findAllById(boardIds);
        assertThat(boards)
            .allSatisfy(board -> assertThat(board.isDeleted()).isTrue());
    }

    @Test
    @DisplayName("삭제 대상에 포함되지 않은 상품은 영향을 받지 않는다")
    void deleteBoards_onlyTargetBoardsAreDeleted() {
        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());
        Board target = BoardFixture.crawlingActiveBoardWithStore(store, "삭제대상");
        Board untouched = BoardFixture.crawlingActiveBoardWithStore(store, "유지대상");

        boardRepository.saveAll(List.of(target, untouched));

        // when
        adminBoardService.deleteBoards(List.of(target.getId()));
        boardRepository.flush();

        // then
        Board deletedBoard = boardRepository.findById(target.getId()).orElseThrow();
        Board notDeletedBoard = boardRepository.findById(untouched.getId()).orElseThrow();

        assertThat(deletedBoard.isDeleted()).isTrue();
        assertThat(notDeletedBoard.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("상품 옵션 전체 삭제 시 해당 Board의 모든 Product가 soft delete 되고 조회되지 않는다")
    void deleteProducts_removeAll_softDeletesAllProductsAndNotVisible() {
        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());
        Board board = boardRepository.save(BoardFixture.crawlingActiveBoardWithStore(store, "테스트상품"));

        Product product1 = productRepository.save(ProductFixture.create(board, "옵션1"));
        Product product2 = productRepository.save(ProductFixture.create(board, "옵션2"));

        // 삭제 전 조회 확인
        Page<AdminProductResponse> beforeDelete = adminBoardService.getAdminBoards(PageRequest.of(0, 10));
        assertThat(beforeDelete.getContent())
            .extracting(AdminProductResponse::productName)
            .contains("테스트상품");

        // when
        RemoveProductsCommand command = new RemoveProductsCommand(board.getId(), true, null);
        adminBoardService.deleteProducts(command);
        productRepository.flush();

        // then
        List<Product> products = productRepository.findAllById(List.of(product1.getId(), product2.getId()));
        assertThat(products)
            .allSatisfy(product -> assertThat(product.isDeleted()).isTrue());
    }

    @Test
    @DisplayName("상품 옵션 선택 삭제 시 지정된 Product만 soft delete 되고 나머지는 유지된다")
    void deleteProducts_selectiveDelete_softDeletesOnlySelectedProducts() {
        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());
        Board board = boardRepository.save(BoardFixture.crawlingActiveBoardWithStore(store, "테스트상품"));

        Product toDelete1 = productRepository.save(ProductFixture.create(board, "삭제대상1"));
        Product toDelete2 = productRepository.save(ProductFixture.create(board, "삭제대상2"));
        Product toKeep = productRepository.save(ProductFixture.create(board, "유지대상"));

        // 삭제 전 조회 확인
        Page<AdminProductResponse> beforeDelete = adminBoardService.getAdminBoards(PageRequest.of(0, 10));
        assertThat(beforeDelete.getContent())
            .extracting(AdminProductResponse::productName)
            .contains("테스트상품");

        // when
        List<Long> productIdsToDelete = List.of(toDelete1.getId(), toDelete2.getId());
        RemoveProductsCommand command = new RemoveProductsCommand(board.getId(), false, productIdsToDelete);
        adminBoardService.deleteProducts(command);
        productRepository.flush();

        // then
        Product deletedProduct1 = productRepository.findById(toDelete1.getId()).orElseThrow();
        Product deletedProduct2 = productRepository.findById(toDelete2.getId()).orElseThrow();
        Product keptProduct = productRepository.findById(toKeep.getId()).orElseThrow();

        assertThat(deletedProduct1.isDeleted()).isTrue();
        assertThat(deletedProduct2.isDeleted()).isTrue();
        assertThat(keptProduct.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("업로드 상품 조회는 PENDING 상태만 필터링된다")
    void getUploadApprovals_filtersByPendingStatus() {
        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());

        Board pending1 = BoardFixture.pendingBoardWithStore(store, "승인대기상품1");
        Board pending2 = BoardFixture.pendingBoardWithStore(store, "승인대기상품2");
        Board outOfStock = BoardFixture.outOfStockBoardWithStore(store, "품절상품");
        Board stopped = BoardFixture.stoppedBoardWithStore(store, "판매중지상품");
        Board banned = BoardFixture.bannedBoardWithStore(store, "판매금지상품");

        boardRepository.saveAll(List.of(pending1, pending2, outOfStock, stopped, banned));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<UploadApprovalResponse> result = adminBoardService.getUploadApprovals(pageable);

        // then
        assertThat(result.getContent())
            .hasSize(2)
            .extracting(UploadApprovalResponse::boardTitle)
            .containsExactlyInAnyOrder("승인대기상품1", "승인대기상품2");

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("업로드 상품 조회는 삭제된 상품을 제외한다")
    void getUploadApprovals_excludesDeleted() {
        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());

        Board pending = BoardFixture.pendingBoardWithStore(store, "정상_승인대기");
        boardRepository.save(pending);

        // 삭제된 PENDING 상품 생성
        Board pendingDeleted = BoardFixture.pendingBoardWithStore(store, "삭제된_승인대기");
        boardRepository.save(pendingDeleted);
        org.springframework.test.util.ReflectionTestUtils.setField(pendingDeleted, "isDeleted", true);
        boardRepository.saveAndFlush(pendingDeleted);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<UploadApprovalResponse> result = adminBoardService.getUploadApprovals(pageable);

        // then
        assertThat(result.getContent())
            .hasSize(1)
            .extracting(UploadApprovalResponse::boardTitle)
            .contains("정상_승인대기");

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("업로드 상품 조회는 페이지네이션을 지원한다")
    void getUploadApprovals_respectsPagination() {
        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());

        for (int i = 0; i < 15; i++) {
            boardRepository.save(BoardFixture.pendingBoardWithStore(store, "상품" + i));
        }

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<UploadApprovalResponse> result = adminBoardService.getUploadApprovals(pageable);

        // then
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getTotalElements()).isEqualTo(15);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("업로드 상품 조회는 생성일자 역순으로 정렬된다")
    void getUploadApprovals_sortsByCreatedAtDesc() {
        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());

        Board older = boardRepository.save(BoardFixture.pendingBoardWithStore(store, "오래된상품"));
        Board newer = boardRepository.save(BoardFixture.pendingBoardWithStore(store, "최신상품"));

        Pageable pageable = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("createdAt").descending());

        // when
        Page<UploadApprovalResponse> result = adminBoardService.getUploadApprovals(pageable);

        // then
        assertThat(result.getContent())
            .hasSize(2)
            .extracting(UploadApprovalResponse::boardTitle)
            .containsExactly("최신상품", "오래된상품");
    }

    @Test
    @DisplayName("조회 대상이 되는 상품이 없으면 빈 페이지를 반환한다")
    void getUploadApprovals_emptyWhenNoValidBoards() {
        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());

        boardRepository.saveAll(
            List.of(
                BoardFixture.outOfStockBoardWithStore(store, "품절상품"),
                BoardFixture.stoppedBoardWithStore(store, "판매중지상품"),
                BoardFixture.bannedBoardWithStore(store, "판매금지상품")
            )
        );

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<UploadApprovalResponse> result = adminBoardService.getUploadApprovals(pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
    }

}