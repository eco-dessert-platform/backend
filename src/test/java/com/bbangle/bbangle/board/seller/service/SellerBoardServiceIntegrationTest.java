package com.bbangle.bbangle.board.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.InventoryStatus;
import com.bbangle.bbangle.board.domain.Nutrition;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.board.domain.SaleStatus;
import com.bbangle.bbangle.board.domain.SortType;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductImgRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.board.seller.service.command.BoardDetailCommand;
import com.bbangle.bbangle.board.seller.service.command.CreateBoardServiceCommand;
import com.bbangle.bbangle.board.seller.service.command.ProductCommand;
import com.bbangle.bbangle.board.seller.service.command.ProductImgCommand;
import com.bbangle.bbangle.board.seller.service.command.ProductInfoNoticeCommand;
import com.bbangle.bbangle.board.seller.service.command.SearchSellerBoardCommand;
import com.bbangle.bbangle.board.seller.service.command.UpdateBoardServiceCommand;
import com.bbangle.bbangle.board.seller.service.command.UpdateProductCommand;
import com.bbangle.bbangle.board.seller.service.info.BoardInfo;
import com.bbangle.bbangle.board.seller.service.info.SellerBoardItemInfo;
import com.bbangle.bbangle.board.seller.service.info.SellerBoardListInfo;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] SellerBoardService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SellerBoardServiceIntegrationTest {

    @Autowired
    private SellerBoardService sellerBoardService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImgRepository productImgRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private EntityManager em;

    private Store store;

    @BeforeEach
    void setUp() {
        store = storeRepository.saveAndFlush(StoreFixture.defaultStore());
    }

    @Nested
    @DisplayName("createBoard 메서드")
    class CreateBoard {

        @DisplayName("정상적인 입력으로 Board가 DB에 저장되고 BoardInfo를 반환한다.")
        @Test
        void givenValidCommand_whenCreateBoard_thenBoardIsPersisted() {
            // given
            CreateBoardServiceCommand command = createValidCommand(store);

            // when
            BoardInfo result = sellerBoardService.createBoard(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.boardId()).isNotNull();
            assertThat(result.title()).isEqualTo("글루텐프리 빵 세트");
            assertThat(result.createdAt()).isNotNull();

            em.flush();
            em.clear();

            Board savedBoard = boardRepository.findById(result.boardId()).orElseThrow();
            assertThat(savedBoard.getTitle()).isEqualTo("글루텐프리 빵 세트");
            assertThat(savedBoard.getPrice()).isEqualTo(15000);
            assertThat(savedBoard.getStore().getId()).isEqualTo(store.getId());
        }

        @DisplayName("Board와 함께 Product가 정상적으로 저장된다.")
        @Test
        void givenCommandWithProducts_whenCreateBoard_thenProductsArePersisted() {
            // given
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

            // when
            BoardInfo result = sellerBoardService.createBoard(command);

            // then
            em.flush();
            em.clear();

            Board savedBoard = boardRepository.findById(result.boardId()).orElseThrow();
            List<Product> products = savedBoard.getProducts();
            assertThat(products).hasSize(2);
            assertThat(products).extracting(Product::getTitle)
                .containsExactlyInAnyOrder("글루텐프리 식빵", "비건 쿠키");
        }

        @DisplayName("Board와 함께 ProductImg가 정상적으로 저장된다.")
        @Test
        void givenCommandWithImages_whenCreateBoard_thenImagesArePersisted() {
            // given
            List<ProductImgCommand> productImgs = List.of(
                new ProductImgCommand("https://cdn.example.com/thumbnail.jpg", 0),
                new ProductImgCommand("https://cdn.example.com/sub1.jpg", 1),
                new ProductImgCommand("https://cdn.example.com/sub2.jpg", 2)
            );

            CreateBoardServiceCommand command = CreateBoardServiceCommand.builder()
                .store(store)
                .title("이미지 포함 상품")
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

            // when
            BoardInfo result = sellerBoardService.createBoard(command);

            // then
            em.flush();
            em.clear();

            Board savedBoard = boardRepository.findById(result.boardId()).orElseThrow();
            List<ProductImg> images = savedBoard.getProductImgs();
            assertThat(images).hasSize(3);
            assertThat(savedBoard.getThumbnail()).isEqualTo("https://cdn.example.com/thumbnail.jpg");
            assertThat(savedBoard.getSupportingImages()).hasSize(2);
        }

        @DisplayName("Board와 함께 ProductInfoNotice가 정상적으로 저장된다.")
        @Test
        void givenCommandWithProductInfoNotice_whenCreateBoard_thenProductInfoNoticeIsPersisted() {
            // given
            CreateBoardServiceCommand command = createValidCommand(store);

            // when
            BoardInfo result = sellerBoardService.createBoard(command);

            // then
            em.flush();
            em.clear();

            Board savedBoard = boardRepository.findById(result.boardId()).orElseThrow();
            assertThat(savedBoard.getProductInfoNotice()).isNotNull();
            assertThat(savedBoard.getProductInfoNotice().getProductName()).isEqualTo("글루텐프리 빵 세트");
            assertThat(savedBoard.getProductInfoNotice().getManufacturer()).isEqualTo("빵그리의 오븐");
        }

        @DisplayName("Board와 함께 BoardDetail이 정상적으로 저장된다.")
        @Test
        void givenCommandWithBoardDetail_whenCreateBoard_thenBoardDetailIsPersisted() {
            // given
            CreateBoardServiceCommand command = createValidCommand(store);

            // when
            BoardInfo result = sellerBoardService.createBoard(command);

            // then
            em.flush();
            em.clear();

            Board savedBoard = boardRepository.findById(result.boardId()).orElseThrow();
            assertThat(savedBoard.getBoardDetail()).isNotNull();
            assertThat(savedBoard.getBoardDetail().getContent()).isEqualTo("<p>상세설명입니다</p>");
        }

        @DisplayName("할인 타입이 RATE일 때 할인율과 할인가가 올바르게 계산된다.")
        @Test
        void givenRateDiscount_whenCreateBoard_thenDiscountCalculatedCorrectly() {
            // given
            CreateBoardServiceCommand command = CreateBoardServiceCommand.builder()
                .store(store)
                .title("할인율 적용 상품")
                .price(20000)
                .discountType("RATE")
                .discountValue(20)
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
                .boardDetail(new BoardDetailCommand("<p>상세설명</p>"))
                .productInfoNotice(createProductInfoNoticeCommand())
                .build();

            // when
            BoardInfo result = sellerBoardService.createBoard(command);

            // then
            em.flush();
            em.clear();

            Board savedBoard = boardRepository.findById(result.boardId()).orElseThrow();
            assertThat(savedBoard.getDiscountRate()).isEqualTo(20);
            assertThat(savedBoard.getDiscountPrice()).isEqualTo(16000);
        }

        @DisplayName("할인 타입이 AMOUNT일 때 할인율과 할인가가 올바르게 계산된다.")
        @Test
        void givenAmountDiscount_whenCreateBoard_thenDiscountCalculatedCorrectly() {
            // given
            CreateBoardServiceCommand command = CreateBoardServiceCommand.builder()
                .store(store)
                .title("정액할인 적용 상품")
                .price(20000)
                .discountType("AMOUNT")
                .discountValue(5000)
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
                .boardDetail(new BoardDetailCommand("<p>상세설명</p>"))
                .productInfoNotice(createProductInfoNoticeCommand())
                .build();

            // when
            BoardInfo result = sellerBoardService.createBoard(command);

            // then
            em.flush();
            em.clear();

            Board savedBoard = boardRepository.findById(result.boardId()).orElseThrow();
            assertThat(savedBoard.getDiscountRate()).isEqualTo(25);
            assertThat(savedBoard.getDiscountPrice()).isEqualTo(15000);
        }

        @DisplayName("타이틀이 비어있으면 INVALID_BOARD_TITLE 예외가 발생한다.")
        @Test
        void givenBlankTitle_whenCreateBoard_thenThrowsException() {
            // given
            CreateBoardServiceCommand command = CreateBoardServiceCommand.builder()
                .store(store)
                .title("")
                .price(15000)
                .discountType("RATE")
                .discountValue(0)
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
                .boardDetail(new BoardDetailCommand("<p>상세설명</p>"))
                .productInfoNotice(createProductInfoNoticeCommand())
                .build();

            // when & then
            assertThatThrownBy(() -> sellerBoardService.createBoard(command))
                .isInstanceOf(BbangleException.class);
        }

        @DisplayName("가격이 음수이면 INVALID_BOARD_PRICE 예외가 발생한다.")
        @Test
        void givenNegativePrice_whenCreateBoard_thenThrowsException() {
            // given
            CreateBoardServiceCommand command = CreateBoardServiceCommand.builder()
                .store(store)
                .title("음수 가격 상품")
                .price(-1000)
                .discountType("RATE")
                .discountValue(0)
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
                .boardDetail(new BoardDetailCommand("<p>상세설명</p>"))
                .productInfoNotice(createProductInfoNoticeCommand())
                .build();

            // when & then
            assertThatThrownBy(() -> sellerBoardService.createBoard(command))
                .isInstanceOf(BbangleException.class);
        }

        @DisplayName("배송비가 음수이면 INVALID_DELIVERY_FEE 예외가 발생한다.")
        @Test
        void givenNegativeDeliveryFee_whenCreateBoard_thenThrowsException() {
            // given
            CreateBoardServiceCommand command = CreateBoardServiceCommand.builder()
                .store(store)
                .title("배송비 음수 상품")
                .price(15000)
                .discountType("RATE")
                .discountValue(0)
                .deliveryFee(-1000)
                .freeShippingConditions(30000)
                .isFresh(false)
                .productionStartTime("T_09_10")
                .deliveryCondition("일반배송")
                .deliveryCompany("CJ대한통운")
                .productImgs(List.of(
                    new ProductImgCommand("https://cdn.example.com/thumbnail.jpg", 0)
                ))
                .products(List.of(createProductCommand("글루텐프리 식빵", "BREAD")))
                .boardDetail(new BoardDetailCommand("<p>상세설명</p>"))
                .productInfoNotice(createProductInfoNoticeCommand())
                .build();

            // when & then
            assertThatThrownBy(() -> sellerBoardService.createBoard(command))
                .isInstanceOf(BbangleException.class);
        }
    }

    @Nested
    @DisplayName("updateBoard 메서드")
    class UpdateBoard {

        private Seller seller;
        private Long boardId;

        @BeforeEach
        void setUpUpdateBoard() {
            seller = sellerRepository.saveAndFlush(SellerFixture.defaultSeller(store));
            BoardInfo created = sellerBoardService.createBoard(createValidCommand(store));
            boardId = created.boardId();
            em.flush();
            em.clear();
        }

        @Test
        @DisplayName("정상적인 입력으로 Board 기본 정보가 수정된다")
        void success_updatesBasicBoardInfo() {
            // given
            UpdateBoardServiceCommand command = createValidUpdateCommand(seller.getId(), boardId);

            // when
            BoardInfo result = sellerBoardService.updateBoard(command);

            // then
            em.flush();
            em.clear();

            Board updatedBoard = boardRepository.findWithAllById(result.boardId()).orElseThrow();
            assertThat(updatedBoard.getTitle()).isEqualTo("수정된 상품명");
            assertThat(updatedBoard.getPrice()).isEqualTo(20000);
            assertThat(updatedBoard.getDeliveryFee()).isEqualTo(2500);
        }

        @Test
        @DisplayName("ProductInfoNotice가 수정된다")
        void success_updatesProductInfoNotice() {
            // given
            UpdateBoardServiceCommand command = createValidUpdateCommand(seller.getId(), boardId);

            // when
            sellerBoardService.updateBoard(command);

            // then
            em.flush();
            em.clear();

            Board updatedBoard = boardRepository.findWithAllById(boardId).orElseThrow();
            assertThat(updatedBoard.getProductInfoNotice().getProductName()).isEqualTo("수정된 상품명");
            assertThat(updatedBoard.getProductInfoNotice().getManufacturer()).isEqualTo("수정 제조사");
        }

        @Test
        @DisplayName("BoardDetail content가 수정된다")
        void success_updatesBoardDetail() {
            // given
            UpdateBoardServiceCommand command = createValidUpdateCommand(seller.getId(), boardId);

            // when
            sellerBoardService.updateBoard(command);

            // then
            em.flush();
            em.clear();

            Board updatedBoard = boardRepository.findWithAllById(boardId).orElseThrow();
            assertThat(updatedBoard.getBoardDetail().getContent()).isEqualTo("<p>수정된 상세내용</p>");
        }

        @Test
        @DisplayName("기존 ProductImg가 soft-delete되고 새 이미지로 교체된다")
        void success_replacesProductImgs() {
            // given
            UpdateBoardServiceCommand command = UpdateBoardServiceCommand.builder()
                .sellerId(seller.getId())
                .boardId(boardId)
                .title("수정된 상품명")
                .price(20000)
                .discountType("RATE")
                .discountValue(0)
                .deliveryFee(2500)
                .freeShippingConditions(50000)
                .isFresh(true)
                .productionStartTime("T_09_10")
                .deliveryCondition("COLD")
                .deliveryCompany("우체국택배")
                .productImgs(List.of(
                    new ProductImgCommand("https://cdn.example.com/new-thumbnail.jpg", 0),
                    new ProductImgCommand("https://cdn.example.com/new-sub.jpg", 1)
                ))
                .products(List.of(createUpdateProductCommand(null)))
                .boardDetail(new BoardDetailCommand("<p>수정된 상세내용</p>"))
                .productInfoNotice(createUpdateNoticeCommand())
                .build();

            // when
            sellerBoardService.updateBoard(command);

            // then
            em.flush();
            em.clear();

            Board updatedBoard = boardRepository.findWithAllById(boardId).orElseThrow();
            List<ProductImg> activeImgs = updatedBoard.getProductImgs().stream()
                .filter(img -> !img.isDeleted())
                .toList();
            assertThat(activeImgs).hasSize(2);
            assertThat(activeImgs).extracting(ProductImg::getUrl)
                .contains("https://cdn.example.com/new-thumbnail.jpg");
        }

        @Test
        @DisplayName("요청에 없는 기존 Product가 soft-delete된다")
        void success_softDeletesRemovedProduct() {
            // given - 기존 board에 product가 1개 있는 상태에서 products를 빈 목록으로 수정
            UpdateBoardServiceCommand command = UpdateBoardServiceCommand.builder()
                .sellerId(seller.getId())
                .boardId(boardId)
                .title("수정된 상품명")
                .price(20000)
                .discountType("RATE")
                .discountValue(0)
                .deliveryFee(2500)
                .freeShippingConditions(50000)
                .isFresh(true)
                .productionStartTime("T_09_10")
                .deliveryCondition("COLD")
                .deliveryCompany("우체국택배")
                .productImgs(List.of(
                    new ProductImgCommand("https://cdn.example.com/thumbnail.jpg", 0)
                ))
                .products(List.of())
                .boardDetail(new BoardDetailCommand("<p>수정된 상세내용</p>"))
                .productInfoNotice(createUpdateNoticeCommand())
                .build();

            // when
            sellerBoardService.updateBoard(command);

            // then
            em.flush();
            em.clear();

            Board updatedBoard = boardRepository.findWithAllById(boardId).orElseThrow();
            long activeProducts = updatedBoard.getProducts().stream()
                .filter(p -> !p.isDeleted())
                .count();
            assertThat(activeProducts).isZero();
        }

        @Test
        @DisplayName("신규 Product가 추가된다")
        void success_addsNewProduct() {
            // given
            UpdateBoardServiceCommand command = UpdateBoardServiceCommand.builder()
                .sellerId(seller.getId())
                .boardId(boardId)
                .title("수정된 상품명")
                .price(20000)
                .discountType("RATE")
                .discountValue(0)
                .deliveryFee(2500)
                .freeShippingConditions(50000)
                .isFresh(true)
                .productionStartTime("T_09_10")
                .deliveryCondition("COLD")
                .deliveryCompany("우체국택배")
                .productImgs(List.of(
                    new ProductImgCommand("https://cdn.example.com/thumbnail.jpg", 0)
                ))
                .products(List.of(
                    createUpdateProductCommand(null),
                    createUpdateProductCommand(null)
                ))
                .boardDetail(new BoardDetailCommand("<p>수정된 상세내용</p>"))
                .productInfoNotice(createUpdateNoticeCommand())
                .build();

            // when
            sellerBoardService.updateBoard(command);

            // then
            em.flush();
            em.clear();

            Board updatedBoard = boardRepository.findWithAllById(boardId).orElseThrow();
            long activeProducts = updatedBoard.getProducts().stream()
                .filter(p -> !p.isDeleted())
                .count();
            assertThat(activeProducts).isEqualTo(2);
        }

        @Test
        @DisplayName("다른 셀러의 Board를 수정하면 FORBIDDEN_BOARD_ACCESS 예외가 발생한다")
        void fail_whenNotOwner_throwsForbiddenException() {
            // given
            Store anotherStore = storeRepository.saveAndFlush(StoreFixture.defaultStore());
            Seller anotherSeller = sellerRepository.saveAndFlush(
                SellerFixture.defaultSeller(anotherStore));
            UpdateBoardServiceCommand command = createValidUpdateCommand(anotherSeller.getId(), boardId);

            // when & then
            assertThatThrownBy(() -> sellerBoardService.updateBoard(command))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.FORBIDDEN_BOARD_ACCESS);
        }

        @Test
        @DisplayName("존재하지 않는 boardId로 수정하면 BOARD_NOT_FOUND 예외가 발생한다")
        void fail_whenBoardNotFound_throwsException() {
            // given
            UpdateBoardServiceCommand command = createValidUpdateCommand(seller.getId(), 999999L);

            // when & then
            assertThatThrownBy(() -> sellerBoardService.updateBoard(command))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.BOARD_NOT_FOUND);
        }

        private UpdateBoardServiceCommand createValidUpdateCommand(Long sellerId, Long boardId) {
            return UpdateBoardServiceCommand.builder()
                .sellerId(sellerId)
                .boardId(boardId)
                .title("수정된 상품명")
                .price(20000)
                .discountType("RATE")
                .discountValue(0)
                .deliveryFee(2500)
                .freeShippingConditions(50000)
                .isFresh(true)
                .productionStartTime("T_09_10")
                .deliveryCondition("COLD")
                .deliveryCompany("우체국택배")
                .productImgs(List.of(
                    new ProductImgCommand("https://cdn.example.com/new-thumbnail.jpg", 0)
                ))
                .products(List.of(createUpdateProductCommand(null)))
                .boardDetail(new BoardDetailCommand("<p>수정된 상세내용</p>"))
                .productInfoNotice(createUpdateNoticeCommand())
                .build();
        }

        private UpdateProductCommand createUpdateProductCommand(Long productId) {
            return UpdateProductCommand.builder()
                .productId(productId)
                .title("수정된 상품옵션")
                .category("BREAD")
                .plusPriceWithBoardPrice(0)
                .stock(50)
                .glutenFreeTag(true)
                .highProteinTag(false)
                .sugarFreeTag(false)
                .veganTag(false)
                .ketogenicTag(false)
                .monday(true)
                .tuesday(true)
                .wednesday(false)
                .thursday(false)
                .friday(false)
                .saturday(false)
                .sunday(false)
                .nutrition(new Nutrition(200, 50, 25, 5, 8, 6, 150))
                .build();
        }

        private ProductInfoNoticeCommand createUpdateNoticeCommand() {
            return ProductInfoNoticeCommand.builder()
                .productName("수정된 상품명")
                .foodType("빵류")
                .manufacturer("수정 제조사")
                .originLocation("부산광역시")
                .manufactureDate("제조일자 별도 표기")
                .expirationDate("제조일로부터 3일")
                .storageGuide("냉장보관")
                .packagingQuantityUnit("1개")
                .rawMaterialName("밀가루, 설탕")
                .nutritionInfo("별도 표기")
                .transgenic("해당없음")
                .customerWarning("알레르기 주의")
                .importFood("해당없음")
                .build();
        }
    }

    @Nested
    @DisplayName("searchBoards 메서드")
    class SearchBoards {

        private Seller seller;

        @BeforeEach
        void setUpSearchBoards() {
            seller = sellerRepository.saveAndFlush(SellerFixture.defaultSeller(store));

            // Board1: ON_SALE, 모든 상품 재고 있음 → IN_STOCK
            createAndSaveBoardForSearch(store, "글루텐프리 식빵", SaleStatus.ON_SALE,
                3000, 30000, "thumb1.jpg", List.of(false, false));

            // Board2: OUT_OF_STOCK, 모든 상품 품절 → SOLDOUT
            createAndSaveBoardForSearch(store, "비건 쿠키", SaleStatus.OUT_OF_STOCK,
                0, null, "thumb2.jpg", List.of(true, true));

            // Board3: PENDING, 일부 상품 품절 → PARTIAL_SOLDOUT
            createAndSaveBoardForSearch(store, "건강 케이크", SaleStatus.PENDING,
                3000, null, "thumb3.jpg", List.of(false, true));

            em.flush();
            em.clear();
        }

        @Test
        @DisplayName("필터 없이 조회하면 해당 스토어의 모든 게시글이 반환된다")
        void searchBoards_withNoFilter_returnsAllBoards() {
            // given
            SearchSellerBoardCommand command = new SearchSellerBoardCommand(
                seller.getId(), null, null, null, null, SortType.LATEST, 0, 10);

            // when
            SellerBoardListInfo result = sellerBoardService.searchBoards(command);

            // then
            assertThat(result.boards().getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("saleStatus 필터를 적용하면 해당 상태의 게시글만 반환된다")
        void searchBoards_withSaleStatusFilter_returnsFilteredBoards() {
            // given
            SearchSellerBoardCommand command = new SearchSellerBoardCommand(
                seller.getId(), SaleStatus.ON_SALE, null, null, null, SortType.LATEST, 0, 10);

            // when
            SellerBoardListInfo result = sellerBoardService.searchBoards(command);

            // then
            assertThat(result.boards().getTotalElements()).isEqualTo(1);
            assertThat(result.boards().getContent().get(0).title()).isEqualTo("글루텐프리 식빵");
        }

        @Test
        @DisplayName("keyword 필터를 적용하면 상품명에 keyword가 포함된 게시글만 반환된다")
        void searchBoards_withKeywordFilter_returnsFilteredBoards() {
            // given
            SearchSellerBoardCommand command = new SearchSellerBoardCommand(
                seller.getId(), null, null, null, "쿠키", SortType.LATEST, 0, 10);

            // when
            SellerBoardListInfo result = sellerBoardService.searchBoards(command);

            // then
            assertThat(result.boards().getTotalElements()).isEqualTo(1);
            assertThat(result.boards().getContent().get(0).title()).isEqualTo("비건 쿠키");
        }

        @Test
        @DisplayName("tabCounts는 스토어 내 각 판매상태별 게시글 수를 반환한다")
        void searchBoards_returnsCorrectTabCounts() {
            // given
            SearchSellerBoardCommand command = new SearchSellerBoardCommand(
                seller.getId(), null, null, null, null, SortType.LATEST, 0, 10);

            // when
            SellerBoardListInfo result = sellerBoardService.searchBoards(command);

            // then
            Map<SaleStatus, Long> tabCounts = result.tabCounts();
            assertThat(tabCounts.get(SaleStatus.ON_SALE)).isEqualTo(1L);
            assertThat(tabCounts.get(SaleStatus.OUT_OF_STOCK)).isEqualTo(1L);
            assertThat(tabCounts.get(SaleStatus.PENDING)).isEqualTo(1L);
            assertThat(tabCounts.get(SaleStatus.STOPPED)).isEqualTo(0L);
            assertThat(tabCounts.get(SaleStatus.BANNED)).isEqualTo(0L);
        }

        @Test
        @DisplayName("모든 상품이 품절인 게시글의 재고상태는 SOLDOUT이다")
        void searchBoards_inventoryStatus_soldoutWhenAllSoldout() {
            // given
            SearchSellerBoardCommand command = new SearchSellerBoardCommand(
                seller.getId(), SaleStatus.OUT_OF_STOCK, null, null, null, SortType.LATEST, 0, 10);

            // when
            SellerBoardListInfo result = sellerBoardService.searchBoards(command);

            // then
            SellerBoardItemInfo item = result.boards().getContent().get(0);
            assertThat(item.inventoryStatus()).isEqualTo(InventoryStatus.SOLDOUT);
        }

        @Test
        @DisplayName("일부 상품만 품절인 게시글의 재고상태는 PARTIAL_SOLDOUT이다")
        void searchBoards_inventoryStatus_partialSoldout() {
            // given
            SearchSellerBoardCommand command = new SearchSellerBoardCommand(
                seller.getId(), SaleStatus.PENDING, null, null, null, SortType.LATEST, 0, 10);

            // when
            SellerBoardListInfo result = sellerBoardService.searchBoards(command);

            // then
            SellerBoardItemInfo item = result.boards().getContent().get(0);
            assertThat(item.inventoryStatus()).isEqualTo(InventoryStatus.PARTIAL_SOLDOUT);
        }

        @Test
        @DisplayName("모든 상품이 재고 있는 게시글의 재고상태는 IN_STOCK이다")
        void searchBoards_inventoryStatus_inStockWhenNoneSoldout() {
            // given
            SearchSellerBoardCommand command = new SearchSellerBoardCommand(
                seller.getId(), SaleStatus.ON_SALE, null, null, null, SortType.LATEST, 0, 10);

            // when
            SellerBoardListInfo result = sellerBoardService.searchBoards(command);

            // then
            SellerBoardItemInfo item = result.boards().getContent().get(0);
            assertThat(item.inventoryStatus()).isEqualTo(InventoryStatus.IN_STOCK);
        }

        @Test
        @DisplayName("다른 스토어의 게시글은 조회되지 않는다")
        void searchBoards_doesNotReturnOtherStoresBoards() {
            // given
            Store otherStore = storeRepository.saveAndFlush(StoreFixture.defaultStore());
            Seller otherSeller = sellerRepository.saveAndFlush(SellerFixture.defaultSeller(otherStore));
            createAndSaveBoardForSearch(otherStore, "다른 스토어 상품", SaleStatus.ON_SALE,
                0, null, "other_thumb.jpg", List.of(false));
            em.flush();
            em.clear();

            SearchSellerBoardCommand command = new SearchSellerBoardCommand(
                seller.getId(), null, null, null, null, SortType.LATEST, 0, 10);

            // when
            SellerBoardListInfo result = sellerBoardService.searchBoards(command);

            // then
            assertThat(result.boards().getTotalElements()).isEqualTo(3);
            assertThat(result.boards().getContent())
                .extracting(SellerBoardItemInfo::title)
                .doesNotContain("다른 스토어 상품");
        }

        @Test
        @DisplayName("NAME 정렬로 조회하면 상품명 오름차순으로 반환된다")
        void searchBoards_sortByName_returnsAlphabetically() {
            // given
            SearchSellerBoardCommand command = new SearchSellerBoardCommand(
                seller.getId(), null, null, null, null, SortType.NAME, 0, 10);

            // when
            SellerBoardListInfo result = sellerBoardService.searchBoards(command);

            // then
            List<String> titles = result.boards().getContent().stream()
                .map(SellerBoardItemInfo::title)
                .toList();
            assertThat(titles).isSortedAccordingTo(String::compareTo);
        }

        @Test
        @DisplayName("페이지 크기보다 게시글이 많으면 totalElements와 totalPages가 올바르게 반환된다")
        void searchBoards_pagination_worksCorrectly() {
            // given
            SearchSellerBoardCommand command = new SearchSellerBoardCommand(
                seller.getId(), null, null, null, null, SortType.LATEST, 0, 2);

            // when
            SellerBoardListInfo result = sellerBoardService.searchBoards(command);

            // then
            assertThat(result.boards().getContent()).hasSize(2);
            assertThat(result.boards().getTotalElements()).isEqualTo(3);
            assertThat(result.boards().getTotalPages()).isEqualTo(2);
        }
    }

    private Board createAndSaveBoardForSearch(
        Store targetStore,
        String title,
        SaleStatus saleStatus,
        Integer deliveryFee,
        Integer freeShippingConditions,
        String thumbnailUrl,
        List<Boolean> soldoutStates
    ) {
        Board board = BoardFixture.defaultBoardWithStore(targetStore, title);
        ReflectionTestUtils.setField(board, "saleStatus", saleStatus);
        ReflectionTestUtils.setField(board, "deliveryFee", deliveryFee);
        ReflectionTestUtils.setField(board, "freeShippingConditions", freeShippingConditions);
        ReflectionTestUtils.setField(board, "isDeleted", false);
        boardRepository.save(board);

        ProductImg thumbnail = ProductImg.builder()
            .board(board)
            .url(thumbnailUrl)
            .imgOrder(0)
            .build();
        productImgRepository.save(thumbnail);

        for (Boolean soldout : soldoutStates) {
            Product product = Product.builder()
                .board(board)
                .title("옵션")
                .soldout(soldout)
                .build();
            productRepository.save(product);
        }

        return board;
    }

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
}
