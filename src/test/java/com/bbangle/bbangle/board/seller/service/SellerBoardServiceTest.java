package com.bbangle.bbangle.board.seller.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Nutrition;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductImgRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardDetailResponse;
import com.bbangle.bbangle.board.seller.controller.mapper.SellerBoardDetailMapper;
import com.bbangle.bbangle.board.seller.service.command.BoardDetailCommand;
import com.bbangle.bbangle.board.seller.service.command.CreateBoardServiceCommand;
import com.bbangle.bbangle.board.seller.service.command.ProductCommand;
import com.bbangle.bbangle.board.seller.service.command.ProductImgCommand;
import com.bbangle.bbangle.board.seller.service.command.ProductInfoNoticeCommand;
import com.bbangle.bbangle.board.seller.service.info.BoardInfo;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("[비즈니스 로직] SellerBoardService")
@ExtendWith(MockitoExtension.class)
class SellerBoardServiceTest {

    @InjectMocks
    private SellerBoardService sut;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ProductImgRepository productImgRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private SellerBoardDetailMapper sellerBoardDetailMapper;

    private CreateBoardServiceCommand createValidCommand(Store store) {
        return CreateBoardServiceCommand.builder()
            .store(store)
            .title("글루텐프리 빵 세트")
            .price(15000)
            .discountType("RATE")
            .discountValue(10)
            .deliveryFee(3000)
            .freeShippingConditions(30000)
            .isFresh(false)
            .productionStartTime("T_09_10")
            .deliveryCondition("일반배송")
            .deliveryCompany("CJ대한통운")
            .productImgs(List.of(
                new ProductImgCommand("https://cdn.example.com/thumbnail.jpg", 0)
            ))
            .products(List.of(createProductCommand("글루텐프리 식빵", "BREAD")))
            .boardDetail(new BoardDetailCommand("<p>상세설명입니다</p>"))
            .productInfoNotice(createProductInfoNoticeCommand())
            .build();
    }

    private ProductCommand createProductCommand(String title, String category) {
        return ProductCommand.builder()
            .title(title)
            .category(category)
            .plusPriceWithBoardPrice(0)
            .stock(100)
            .glutenFreeTag(true)
            .highProteinTag(false)
            .sugarFreeTag(true)
            .veganTag(false)
            .ketogenicTag(false)
            .monday(true)
            .tuesday(true)
            .wednesday(true)
            .thursday(true)
            .friday(true)
            .saturday(false)
            .sunday(false)
            .nutrition(new Nutrition(300, 50, 30, 5, 10, 8, 200))
            .build();
    }

    private ProductInfoNoticeCommand createProductInfoNoticeCommand() {
        return ProductInfoNoticeCommand.builder()
            .productName("글루텐프리 빵 세트")
            .foodType("빵류")
            .manufacturer("빵그리의 오븐")
            .originLocation("서울특별시")
            .manufactureDate("제조일자 별도 표기")
            .expirationDate("제조일로부터 5일")
            .storageGuide("냉동보관")
            .packagingQuantityUnit("1세트")
            .rawMaterialName("쌀가루, 설탕, 버터")
            .nutritionInfo("별도 표기")
            .transgenic("해당없음")
            .customerWarning("알레르기 주의")
            .importFood("해당없음")
            .build();
    }

    @Nested
    @DisplayName("createBoard 메서드")
    class CreateBoard {

        @DisplayName("정상적인 입력으로 Board를 생성하고 BoardInfo를 반환한다.")
        @Test
        void givenValidCommand_whenCreateBoard_thenReturnsBoardInfo() {
            // given
            Store store = StoreFixture.defaultStore();
            ReflectionTestUtils.setField(store, "id", 1L);

            CreateBoardServiceCommand command = createValidCommand(store);

            given(boardRepository.save(any(Board.class)))
                .willAnswer(invocation -> {
                    Board savedBoard = invocation.getArgument(0);
                    ReflectionTestUtils.setField(savedBoard, "id", 100L);
                    return savedBoard;
                });

            // when
            BoardInfo result = sut.createBoard(command);

            // then
            then(boardRepository).should(times(1)).save(any(Board.class));
            assertThat(result).isNotNull();
            assertThat(result.boardId()).isEqualTo(100L);
            assertThat(result.title()).isEqualTo("글루텐프리 빵 세트");
        }

        @DisplayName("여러 상품(Product)을 포함한 Board를 생성한다.")
        @Test
        void givenMultipleProducts_whenCreateBoard_thenBoardContainsAllProducts() {
            // given
            Store store = StoreFixture.defaultStore();
            ReflectionTestUtils.setField(store, "id", 1L);

            ProductCommand product1 = createProductCommand("글루텐프리 식빵", "BREAD");
            ProductCommand product2 = createProductCommand("비건 쿠키", "COOKIE");

            CreateBoardServiceCommand command = CreateBoardServiceCommand.builder()
                .store(store)
                .title("비건 베이커리 세트")
                .price(25000)
                .discountType("RATE")
                .discountValue(10)
                .deliveryFee(3000)
                .freeShippingConditions(30000)
                .isFresh(false)
                .productionStartTime("T_09_10")
                .deliveryCondition("일반배송")
                .deliveryCompany("CJ대한통운")
                .productImgs(List.of(
                    new ProductImgCommand("https://cdn.example.com/thumbnail.jpg", 0)
                ))
                .products(List.of(product1, product2))
                .boardDetail(new BoardDetailCommand("<p>상세설명</p>"))
                .productInfoNotice(createProductInfoNoticeCommand())
                .build();

            given(boardRepository.save(any(Board.class)))
                .willAnswer(invocation -> {
                    Board savedBoard = invocation.getArgument(0);
                    ReflectionTestUtils.setField(savedBoard, "id", 100L);
                    return savedBoard;
                });

            // when
            BoardInfo result = sut.createBoard(command);

            // then
            then(boardRepository).should(times(1)).save(any(Board.class));
            assertThat(result).isNotNull();
            assertThat(result.title()).isEqualTo("비건 베이커리 세트");
        }

        @DisplayName("여러 이미지를 포함한 Board를 생성한다.")
        @Test
        void givenMultipleImages_whenCreateBoard_thenBoardContainsAllImages() {
            // given
            Store store = StoreFixture.defaultStore();
            ReflectionTestUtils.setField(store, "id", 1L);

            List<ProductImgCommand> productImgs = List.of(
                new ProductImgCommand("https://cdn.example.com/thumbnail.jpg", 0),
                new ProductImgCommand("https://cdn.example.com/sub1.jpg", 1),
                new ProductImgCommand("https://cdn.example.com/sub2.jpg", 2)
            );

            CreateBoardServiceCommand command = CreateBoardServiceCommand.builder()
                .store(store)
                .title("글루텐프리 빵 세트")
                .price(15000)
                .discountType("RATE")
                .discountValue(0)
                .deliveryFee(3000)
                .freeShippingConditions(30000)
                .isFresh(false)
                .productionStartTime("T_09_10")
                .deliveryCondition("일반배송")
                .deliveryCompany("CJ대한통운")
                .productImgs(productImgs)
                .products(List.of(createProductCommand("글루텐프리 식빵", "BREAD")))
                .boardDetail(new BoardDetailCommand("<p>상세설명</p>"))
                .productInfoNotice(createProductInfoNoticeCommand())
                .build();

            given(boardRepository.save(any(Board.class)))
                .willAnswer(invocation -> {
                    Board savedBoard = invocation.getArgument(0);
                    ReflectionTestUtils.setField(savedBoard, "id", 100L);
                    return savedBoard;
                });

            // when
            BoardInfo result = sut.createBoard(command);

            // then
            then(boardRepository).should(times(1)).save(any(Board.class));
            assertThat(result).isNotNull();
        }

        @DisplayName("할인 타입이 AMOUNT일 때 Board를 정상 생성한다.")
        @Test
        void givenAmountDiscount_whenCreateBoard_thenReturnsBoardInfo() {
            // given
            Store store = StoreFixture.defaultStore();
            ReflectionTestUtils.setField(store, "id", 1L);

            CreateBoardServiceCommand command = CreateBoardServiceCommand.builder()
                .store(store)
                .title("할인 상품")
                .price(20000)
                .discountType("AMOUNT")
                .discountValue(5000)
                .deliveryFee(3000)
                .freeShippingConditions(30000)
                .isFresh(true)
                .productionStartTime("T_06_07")
                .deliveryCondition("새벽배송")
                .deliveryCompany("한진택배")
                .productImgs(List.of(
                    new ProductImgCommand("https://cdn.example.com/thumbnail.jpg", 0)
                ))
                .products(List.of(createProductCommand("할인 빵", "BREAD")))
                .boardDetail(new BoardDetailCommand("<p>할인 상품 상세</p>"))
                .productInfoNotice(createProductInfoNoticeCommand())
                .build();

            given(boardRepository.save(any(Board.class)))
                .willAnswer(invocation -> {
                    Board savedBoard = invocation.getArgument(0);
                    ReflectionTestUtils.setField(savedBoard, "id", 100L);
                    return savedBoard;
                });

            // when
            BoardInfo result = sut.createBoard(command);

            // then
            then(boardRepository).should(times(1)).save(any(Board.class));
            assertThat(result).isNotNull();
            assertThat(result.title()).isEqualTo("할인 상품");
        }
    }

    @Nested
    @DisplayName("getBoardDetail() 테스트")
    class GetBoardDetailTest {

        @Test
        @DisplayName("소유권이 확인되면 이미지/옵션을 조회하여 상세 응답을 반환한다.")
        void success_getBoardDetail() {

            // given
            Long sellerId = 1L;
            Long boardId = 10L;
            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 100L);
            Board board = BoardFixture.withId(BoardFixture.defaultBoardWithStore(store, "상세조회 게시글"), boardId);

            List<ProductImg> productImgs = List.of(mock(ProductImg.class));
            List<Product> products = List.of(mock(Product.class));
            SellerBoardDetailResponse response = mock(SellerBoardDetailResponse.class);

            given(boardRepository.findByIdAndIsDeletedFalse(boardId)).willReturn(Optional.of(board));
            given(sellerRepository.existsByIdAndStore_IdAndIsDeletedFalse(sellerId, store.getId())).willReturn(true);
            given(productImgRepository.findAllByBoardIdAndIsDeletedFalseOrderByImgOrderAsc(boardId)).willReturn(productImgs);
            given(productRepository.findAllByBoardIdAndIsDeletedFalse(boardId)).willReturn(products);
            given(sellerBoardDetailMapper.toResponse(board, productImgs, products)).willReturn(response);

            // when
            SellerBoardDetailResponse result = sut.getBoardDetail(sellerId, boardId);

            // then
            assertThat(result).isEqualTo(response);
            verify(sellerBoardDetailMapper).toResponse(board, productImgs, products);
        }

        @Test
        @DisplayName("게시글이 존재하지 않으면 BOARD_NOT_FOUND 예외가 발생하고, 이후 조회는 실행되지 않는다.")
        void fail_boardNotFound() {

            // given
            Long sellerId = 1L;
            Long boardId = 999L;

            given(boardRepository.findByIdAndIsDeletedFalse(boardId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.getBoardDetail(sellerId, boardId))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.BOARD_NOT_FOUND);
                });

            verify(sellerRepository, never()).existsByIdAndStore_IdAndIsDeletedFalse(any(), any());
            verify(productImgRepository, never()).findAllByBoardIdAndIsDeletedFalseOrderByImgOrderAsc(any());
            verify(productRepository, never()).findAllByBoardIdAndIsDeletedFalse(any());
        }

        @Test
        @DisplayName("게시글의 소유자가 아니면 FORBIDDEN_BOARD_ACCESS 예외가 발생하고, 이미지/옵션 조회는 실행되지 않는다.")
        void fail_forbiddenAccess() {

            // given
            Long sellerId = 1L;
            Long boardId = 10L;
            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 100L);
            Board board = BoardFixture.withId(BoardFixture.defaultBoardWithStore(store, "다른 판매자 게시글"), boardId);

            given(boardRepository.findByIdAndIsDeletedFalse(boardId)).willReturn(Optional.of(board));
            given(sellerRepository.existsByIdAndStore_IdAndIsDeletedFalse(sellerId, store.getId())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> sut.getBoardDetail(sellerId, boardId))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.FORBIDDEN_BOARD_ACCESS);
                });

            verify(productImgRepository, never()).findAllByBoardIdAndIsDeletedFalseOrderByImgOrderAsc(any());
            verify(productRepository, never()).findAllByBoardIdAndIsDeletedFalse(any());
            verify(sellerBoardDetailMapper, never()).toResponse(any(), any(), any());
        }
    }
}
