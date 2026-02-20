package com.bbangle.bbangle.board.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Nutrition;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.seller.service.command.BoardDetailCommand;
import com.bbangle.bbangle.board.seller.service.command.CreateBoardServiceCommand;
import com.bbangle.bbangle.board.seller.service.command.ProductCommand;
import com.bbangle.bbangle.board.seller.service.command.ProductImgCommand;
import com.bbangle.bbangle.board.seller.service.command.ProductInfoNoticeCommand;
import com.bbangle.bbangle.board.seller.service.info.BoardInfo;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
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
    private StoreRepository storeRepository;

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
