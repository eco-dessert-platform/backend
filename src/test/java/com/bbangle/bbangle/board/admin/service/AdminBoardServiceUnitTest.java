package com.bbangle.bbangle.board.admin.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.bbangle.bbangle.board.admin.controller.dto.AdminProductResponse;
import com.bbangle.bbangle.board.admin.controller.dto.UploadApprovalResponse;
import com.bbangle.bbangle.board.admin.controller.dto.request.UploadApprovalDecisionRequest;
import com.bbangle.bbangle.board.admin.service.dto.RemoveProductsCommand;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.RejectionCategory;
import com.bbangle.bbangle.board.domain.SaleStatus;
import com.bbangle.bbangle.board.repository.BoardDetailRepository;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductImgRepository;
import com.bbangle.bbangle.board.repository.ProductInfoNoticeRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.boardstatistic.repository.BoardStatisticRepository;
import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
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

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImgRepository productImgRepository;

    @Mock
    private BoardDetailRepository boardDetailRepository;

    @Mock
    private BoardStatisticRepository boardStatisticRepository;

    @Mock
    private ProductInfoNoticeRepository productInfoNoticeRepository;

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

    @Test
    @DisplayName("게시글 삭제 시 연관된 모든 엔티티를 soft delete 처리한다")
    void deleteBoards_success() {
        // given
        List<Long> boardIds = List.of(1L, 2L, 3L);

        // when
        sut.deleteBoards(boardIds);

        // then
        then(boardRepository).should().softDeleteByIds(boardIds);
        then(productRepository).should().softDeleteByBoardIds(boardIds);
        then(productImgRepository).should().softDeleteByBoardIds(boardIds);
        then(boardDetailRepository).should().softDeleteByBoardIds(boardIds);
        then(boardStatisticRepository).should().softDeleteByBoardIds(boardIds);
        then(productInfoNoticeRepository).should().softDeleteByBoardIds(boardIds);
    }

    @Test
    @DisplayName("removeAll이 true이면 boardId로 전체 상품을 soft delete 한다")
    void deleteProducts_removeAll_true() {
        // given
        Long boardId = 1L;
        RemoveProductsCommand command = new RemoveProductsCommand(boardId, true, null);

        // when
        sut.deleteProducts(command);

        // then
        then(productRepository).should().softDeleteByBoardId(boardId);
        then(productRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("removeAll이 false이면 productIds로 선택된 상품만 soft delete 한다")
    void deleteProducts_removeAll_false() {
        // given
        Long boardId = 1L;
        List<Long> productIds = List.of(10L, 20L, 30L);
        RemoveProductsCommand command = new RemoveProductsCommand(boardId, false, productIds);

        // when
        sut.deleteProducts(command);

        // then
        then(productRepository).should().softDeleteByProductIds(productIds);
        then(productRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("업로드 대기 상품을 페이징 형태로 조회한다")
    void getUploadApprovals_success() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Board board = BoardFixture.defaultBoard();
        Page<Board> boardPage = new PageImpl<>(List.of(board), pageable, 1);

        given(boardRepository.findBySaleStatusAndIsDeletedFalse(SaleStatus.PENDING, pageable))
            .willReturn(boardPage);

        // when
        Page<UploadApprovalResponse> result = sut.getUploadApprovals(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        UploadApprovalResponse response = result.getContent().get(0);
        assertThat(response.boardId()).isEqualTo(board.getId());
        assertThat(response.boardTitle()).isEqualTo(board.getTitle());
        assertThat(response.storeName()).isEqualTo(board.getStore().getName());

        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(20);
        assertThat(result.getTotalElements()).isEqualTo(1);
        then(boardRepository).should().findBySaleStatusAndIsDeletedFalse(SaleStatus.PENDING, pageable);
    }

    @Test
    @DisplayName("업로드 대기 상품 조회 결과가 없으면 빈 Page를 반환한다")
    void getUploadApprovals_emptyResult() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Board> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        given(boardRepository.findBySaleStatusAndIsDeletedFalse(SaleStatus.PENDING, pageable))
            .willReturn(emptyPage);

        // when
        Page<UploadApprovalResponse> result = sut.getUploadApprovals(pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        then(boardRepository).should().findBySaleStatusAndIsDeletedFalse(SaleStatus.PENDING, pageable);
    }

    @Test
    @DisplayName("업로드 상품 승인 - 존재하는 Board를 조회하고 approve()를 호출한다")
    void decideUploadApproval_approve_success() {
        // given
        Long boardId = 1L;
        Board board = BoardFixture.pendingBoard();
        UploadApprovalDecisionRequest request = new UploadApprovalDecisionRequest(
            DecisionType.APPROVE,
            null,
            null
        );

        given(boardRepository.findById(boardId)).willReturn(Optional.of(board));

        // when
        sut.decideUploadApproval(boardId, request);

        // then
        assertThat(board.getSaleStatus()).isEqualTo(SaleStatus.ON_SALE);
        then(boardRepository).should().findById(boardId);
        then(boardRepository).should().save(board);
    }

    @Test
    @DisplayName("업로드 상품 거절 - 존재하는 Board를 조회하고 reject()를 호출한다")
    void decideUploadApproval_reject_success() {
        // given
        Long boardId = 2L;
        Board board = BoardFixture.pendingBoard();
        UploadApprovalDecisionRequest request = new UploadApprovalDecisionRequest(
            DecisionType.REJECT,
            RejectionCategory.INAPPROPRIATE_BRAND_NAME,
            "브랜드명을 무단으로 사용하여 고객에게 혼동을 줄 수 있습니다."
        );

        given(boardRepository.findById(boardId)).willReturn(Optional.of(board));

        // when
        sut.decideUploadApproval(boardId, request);

        // then
        assertThat(board.getSaleStatus()).isEqualTo(SaleStatus.BANNED);
        assertThat(board.getRejectionCategory()).isEqualTo(RejectionCategory.INAPPROPRIATE_BRAND_NAME);
        assertThat(board.getRejectionReason()).isEqualTo("브랜드명을 무단으로 사용하여 고객에게 혼동을 줄 수 있습니다.");
        assertThat(board.getRejectionAt()).isNotNull();
        then(boardRepository).should().findById(boardId);
        then(boardRepository).should().save(board);
    }

    @Test
    @DisplayName("업로드 상품 승인/거절 - 존재하지 않는 Board일 경우 BOARD_NOT_FOUND 예외가 발생한다")
    void decideUploadApproval_notFound_throwsException() {
        // given
        Long boardId = 999L;
        UploadApprovalDecisionRequest request = new UploadApprovalDecisionRequest(
            DecisionType.APPROVE,
            null,
            null
        );

        given(boardRepository.findById(boardId)).willReturn(Optional.empty());

        // when & then
        assertThat(
            Assertions.catchThrowableOfType(
                () -> sut.decideUploadApproval(boardId, request),
                BbangleException.class
            )
        ).isNotNull();

        then(boardRepository).should().findById(boardId);
        then(boardRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("업로드 상품 승인 - PENDING이 아닌 상태에서는 INVALID_BOARD_STATUS 예외가 발생한다")
    void decideUploadApproval_approve_nonPendingStatus_throwsException() {
        // given
        Long boardId = 3L;
        Board board = BoardFixture.defaultBoardWithStore(StoreFixture.defaultStore(), "판매중상품");
        UploadApprovalDecisionRequest request = new UploadApprovalDecisionRequest(
            DecisionType.APPROVE,
            null,
            null
        );

        given(boardRepository.findById(boardId)).willReturn(Optional.of(board));

        // when & then
        assertThat(
            org.assertj.core.api.Assertions.catchThrowableOfType(
                () -> sut.decideUploadApproval(boardId, request),
                BbangleException.class
            )
        ).isNotNull();

        then(boardRepository).should().findById(boardId);
        then(boardRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("업로드 상품 거절 - PENDING이 아닌 상태에서는 INVALID_BOARD_STATUS 예외가 발생한다")
    void decideUploadApproval_reject_nonPendingStatus_throwsException() {
        // given
        Long boardId = 4L;
        Board board = BoardFixture.bannedBoardWithStore(StoreFixture.defaultStore(), "판매금지상품");
        UploadApprovalDecisionRequest request = new UploadApprovalDecisionRequest(
            DecisionType.REJECT,
            RejectionCategory.INAPPROPRIATE_BRAND_NAME,
            "이미 거절된 상품입니다."
        );

        given(boardRepository.findById(boardId)).willReturn(Optional.of(board));

        // when & then
        assertThat(
            Assertions.catchThrowableOfType(
                () -> sut.decideUploadApproval(boardId, request),
                BbangleException.class
            )
        ).isNotNull();

        then(boardRepository).should().findById(boardId);
        then(boardRepository).shouldHaveNoMoreInteractions();
    }

}