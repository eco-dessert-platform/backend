package com.bbangle.bbangle.board.seller.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.seller.controller.dto.request.AvailabilityRequest;
import com.bbangle.bbangle.board.seller.controller.dto.request.BoardDetailRequest;
import com.bbangle.bbangle.board.seller.controller.dto.request.DietaryTagsRequest;
import com.bbangle.bbangle.board.seller.controller.dto.request.NutritionInfoRequest;
import com.bbangle.bbangle.board.seller.controller.dto.request.ProductInfoNoticeRequest;
import com.bbangle.bbangle.board.seller.controller.dto.request.ProductRequest;
import com.bbangle.bbangle.board.seller.facade.command.CreateBoardFacadeCommand;
import com.bbangle.bbangle.board.seller.service.info.BoardInfo;
import com.bbangle.bbangle.config.S3IntegrationTestSupport;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] SellerBoardFacade")
@Transactional
class SellerBoardFacadeIntegrationTest extends S3IntegrationTestSupport {

    @Autowired
    private SellerBoardFacade sellerBoardFacade;

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
    @DisplayName("createBoard() 테스트")
    class CreateBoardTest {

        @DisplayName("정상적인 입력으로 Board가 생성되고 이미지가 S3에 업로드된다.")
        @Test
        void givenValidCommand_whenCreateBoard_thenBoardCreatedWithUploadedImages() {
            // given
            CreateBoardFacadeCommand command = createValidCommand(store.getId());

            // when
            BoardInfo result = sellerBoardFacade.createBoard(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.boardId()).isNotNull();
            assertThat(result.title()).isEqualTo("글루텐프리 빵 세트");

            em.flush();
            em.clear();

            Board savedBoard = boardRepository.findById(result.boardId()).orElseThrow();
            assertThat(savedBoard.getTitle()).isEqualTo("글루텐프리 빵 세트");
            assertThat(savedBoard.getPrice()).isEqualTo(15000);
            assertThat(savedBoard.getStore().getId()).isEqualTo(store.getId());
        }

        @DisplayName("썸네일 이미지와 서브 이미지가 모두 업로드되어 Board에 저장된다.")
        @Test
        void givenThumbnailAndSubImages_whenCreateBoard_thenAllImagesUploaded() {
            // given
            MockMultipartFile thumbnail = createMockImage("thumbnail.png");
            MockMultipartFile subImg1 = createMockImage("sub1.png");
            MockMultipartFile subImg2 = createMockImage("sub2.png");

            CreateBoardFacadeCommand command = CreateBoardFacadeCommand.builder()
                .sellerId(1L)
                .storeId(store.getId())
                .title("이미지 테스트 상품")
                .price(20000)
                .discountType("RATE")
                .discountValue(10)
                .deliveryFee(3000)
                .freeShippingConditions(30000)
                .isFresh(false)
                .productionStartTime("T_09_10")
                .deliveryCondition("일반배송")
                .deliveryCompany("CJ대한통운")
                .thumbnailImgFile(thumbnail)
                .productImgs(List.of(subImg1, subImg2))
                .boardDetailImages(null)
                .products(List.of(createProductRequest()))
                .boardDetailRequest(new BoardDetailRequest("<p>상세설명</p>"))
                .productInfoNoticeRequest(createProductInfoNoticeRequest())
                .build();

            // when
            BoardInfo result = sellerBoardFacade.createBoard(command);

            // then
            em.flush();
            em.clear();

            Board savedBoard = boardRepository.findById(result.boardId()).orElseThrow();
            List<ProductImg> images = savedBoard.getProductImgs();
            assertThat(images).hasSize(3);
            assertThat(savedBoard.getThumbnail()).isNotBlank();
            assertThat(savedBoard.getSupportingImages()).hasSize(2);
        }

        @DisplayName("서브 이미지가 없어도 썸네일만으로 Board가 정상 생성된다.")
        @Test
        void givenOnlyThumbnail_whenCreateBoard_thenBoardCreatedWithThumbnailOnly() {
            // given
            CreateBoardFacadeCommand command = CreateBoardFacadeCommand.builder()
                .sellerId(1L)
                .storeId(store.getId())
                .title("썸네일만 있는 상품")
                .price(10000)
                .discountType("RATE")
                .discountValue(0)
                .deliveryFee(3000)
                .freeShippingConditions(30000)
                .isFresh(false)
                .productionStartTime("T_09_10")
                .deliveryCondition("일반배송")
                .deliveryCompany("CJ대한통운")
                .thumbnailImgFile(createMockImage("thumbnail.png"))
                .productImgs(null)
                .boardDetailImages(null)
                .products(List.of(createProductRequest()))
                .boardDetailRequest(new BoardDetailRequest("<p>상세설명</p>"))
                .productInfoNoticeRequest(createProductInfoNoticeRequest())
                .build();

            // when
            BoardInfo result = sellerBoardFacade.createBoard(command);

            // then
            em.flush();
            em.clear();

            Board savedBoard = boardRepository.findById(result.boardId()).orElseThrow();
            assertThat(savedBoard.getProductImgs()).hasSize(1);
            assertThat(savedBoard.getThumbnail()).isNotBlank();
            assertThat(savedBoard.getSupportingImages()).isEmpty();
        }

        @DisplayName("상세 이미지가 없으면 boardDetail content가 그대로 저장된다.")
        @Test
        void givenNoBoardDetailImages_whenCreateBoard_thenContentSavedAsIs() {
            // given
            String originalContent = "<p>원본 상세 설명</p>";

            CreateBoardFacadeCommand command = CreateBoardFacadeCommand.builder()
                .sellerId(1L)
                .storeId(store.getId())
                .title("상세이미지 없는 상품")
                .price(15000)
                .discountType("RATE")
                .discountValue(0)
                .deliveryFee(3000)
                .freeShippingConditions(30000)
                .isFresh(false)
                .productionStartTime("T_09_10")
                .deliveryCondition("일반배송")
                .deliveryCompany("CJ대한통운")
                .thumbnailImgFile(createMockImage("thumbnail.png"))
                .productImgs(null)
                .boardDetailImages(null)
                .products(List.of(createProductRequest()))
                .boardDetailRequest(new BoardDetailRequest(originalContent))
                .productInfoNoticeRequest(createProductInfoNoticeRequest())
                .build();

            // when
            BoardInfo result = sellerBoardFacade.createBoard(command);

            // then
            em.flush();
            em.clear();

            Board savedBoard = boardRepository.findById(result.boardId()).orElseThrow();
            assertThat(savedBoard.getBoardDetail().getContent()).isEqualTo(originalContent);
        }

        @DisplayName("상세 이미지가 있으면 HTML content 내 이미지가 CDN URL로 변환된다.")
        @Test
        void givenBoardDetailImages_whenCreateBoard_thenContentImageUrlsConverted() {
            // given
            String uuid = "detail-img-uuid.png";
            MockMultipartFile detailImage = new MockMultipartFile(
                "boardDetailImages",
                uuid,
                "image/png",
                "detail image content".getBytes()
            );

            String htmlContent = "<p>상세 설명</p><img data-id=\"" + uuid + "\" src=\"blob:temp\">";

            CreateBoardFacadeCommand command = CreateBoardFacadeCommand.builder()
                .sellerId(1L)
                .storeId(store.getId())
                .title("상세이미지 포함 상품")
                .price(15000)
                .discountType("RATE")
                .discountValue(0)
                .deliveryFee(3000)
                .freeShippingConditions(30000)
                .isFresh(false)
                .productionStartTime("T_09_10")
                .deliveryCondition("일반배송")
                .deliveryCompany("CJ대한통운")
                .thumbnailImgFile(createMockImage("thumbnail.png"))
                .productImgs(null)
                .boardDetailImages(List.of(detailImage))
                .products(List.of(createProductRequest()))
                .boardDetailRequest(new BoardDetailRequest(htmlContent))
                .productInfoNoticeRequest(createProductInfoNoticeRequest())
                .build();

            // when
            BoardInfo result = sellerBoardFacade.createBoard(command);

            // then
            em.flush();
            em.clear();

            Board savedBoard = boardRepository.findById(result.boardId()).orElseThrow();
            String savedContent = savedBoard.getBoardDetail().getContent();
            assertThat(savedContent).doesNotContain("blob:temp");
            assertThat(savedContent).doesNotContain("data-id");
        }

        @DisplayName("여러 상품(Product)이 포함된 Board가 정상 생성된다.")
        @Test
        void givenMultipleProducts_whenCreateBoard_thenAllProductsPersisted() {
            // given
            ProductRequest product1 = new ProductRequest(
                "BREAD", "글루텐프리 식빵",
                new DietaryTagsRequest(true, false, true, false, false),
                0, 100,
                new AvailabilityRequest(true, true, true, true, true, false, false),
                new NutritionInfoRequest(300, 50, 30, 5, 10, 8, 200)
            );
            ProductRequest product2 = new ProductRequest(
                "COOKIE", "비건 쿠키",
                new DietaryTagsRequest(false, false, false, true, false),
                2000, 50,
                new AvailabilityRequest(true, true, true, true, true, true, true),
                new NutritionInfoRequest(200, 30, 20, 3, 5, 4, 150)
            );

            CreateBoardFacadeCommand command = CreateBoardFacadeCommand.builder()
                .sellerId(1L)
                .storeId(store.getId())
                .title("다중 상품 세트")
                .price(25000)
                .discountType("AMOUNT")
                .discountValue(5000)
                .deliveryFee(3000)
                .freeShippingConditions(30000)
                .isFresh(false)
                .productionStartTime("T_09_10")
                .deliveryCondition("일반배송")
                .deliveryCompany("CJ대한통운")
                .thumbnailImgFile(createMockImage("thumbnail.png"))
                .productImgs(null)
                .boardDetailImages(null)
                .products(List.of(product1, product2))
                .boardDetailRequest(new BoardDetailRequest("<p>상세설명</p>"))
                .productInfoNoticeRequest(createProductInfoNoticeRequest())
                .build();

            // when
            BoardInfo result = sellerBoardFacade.createBoard(command);

            // then
            em.flush();
            em.clear();

            Board savedBoard = boardRepository.findById(result.boardId()).orElseThrow();
            assertThat(savedBoard.getProducts()).hasSize(2);
        }

        @DisplayName("content에 script 태그가 포함되면 sanitize되어 저장된다.")
        @Test
        void givenXssContent_whenCreateBoard_thenScriptTagIsSanitized() {
            // given
            String xssContent = "<p>정상 내용</p><script>alert('xss')</script>";

            CreateBoardFacadeCommand command = CreateBoardFacadeCommand.builder()
                .sellerId(1L)
                .storeId(store.getId())
                .title("XSS 테스트 상품")
                .price(15000)
                .discountType("RATE")
                .discountValue(0)
                .deliveryFee(3000)
                .freeShippingConditions(30000)
                .isFresh(false)
                .productionStartTime("T_09_10")
                .deliveryCondition("일반배송")
                .deliveryCompany("CJ대한통운")
                .thumbnailImgFile(createMockImage("thumbnail.png"))
                .productImgs(null)
                .boardDetailImages(null)
                .products(List.of(createProductRequest()))
                .boardDetailRequest(new BoardDetailRequest(xssContent))
                .productInfoNoticeRequest(createProductInfoNoticeRequest())
                .build();

            // when
            BoardInfo result = sellerBoardFacade.createBoard(command);

            // then
            em.flush();
            em.clear();

            Board savedBoard = boardRepository.findById(result.boardId()).orElseThrow();
            assertThat(savedBoard.getBoardDetail().getContent()).doesNotContain("<script>");
            assertThat(savedBoard.getBoardDetail().getContent()).contains("정상 내용");
        }

        @DisplayName("존재하지 않는 storeId로 요청하면 STORE_NOT_FOUND 예외가 발생한다.")
        @Test
        void givenNonExistingStoreId_whenCreateBoard_thenThrowsException() {
            // given
            CreateBoardFacadeCommand command = CreateBoardFacadeCommand.builder()
                .sellerId(1L)
                .storeId(999999L)
                .title("존재하지 않는 스토어")
                .price(15000)
                .discountType("RATE")
                .discountValue(0)
                .deliveryFee(3000)
                .freeShippingConditions(30000)
                .isFresh(false)
                .productionStartTime("T_09_10")
                .deliveryCondition("일반배송")
                .deliveryCompany("CJ대한통운")
                .thumbnailImgFile(createMockImage("thumbnail.png"))
                .productImgs(null)
                .boardDetailImages(null)
                .products(List.of(createProductRequest()))
                .boardDetailRequest(new BoardDetailRequest("<p>상세설명</p>"))
                .productInfoNoticeRequest(createProductInfoNoticeRequest())
                .build();

            // when & then
            assertThatThrownBy(() -> sellerBoardFacade.createBoard(command))
                .isInstanceOf(BbangleException.class);
        }

        @DisplayName("Board 생성 중 예외가 발생하면 이미 업로드된 S3 이미지가 롤백된다.")
        @Test
        void givenInvalidBoardData_whenCreateBoard_thenUploadsAreRolledBack() {
            // given - 타이틀이 비어있어 Board.sellerCreate에서 예외 발생
            CreateBoardFacadeCommand command = CreateBoardFacadeCommand.builder()
                .sellerId(1L)
                .storeId(store.getId())
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
                .thumbnailImgFile(createMockImage("thumbnail.png"))
                .productImgs(null)
                .boardDetailImages(null)
                .products(List.of(createProductRequest()))
                .boardDetailRequest(new BoardDetailRequest("<p>상세설명</p>"))
                .productInfoNoticeRequest(createProductInfoNoticeRequest())
                .build();

            // when & then
            assertThatThrownBy(() -> sellerBoardFacade.createBoard(command))
                .isInstanceOf(BbangleException.class);
        }
    }

    private CreateBoardFacadeCommand createValidCommand(Long storeId) {
        return CreateBoardFacadeCommand.builder()
            .sellerId(1L)
            .storeId(storeId)
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
            .thumbnailImgFile(createMockImage("thumbnail.png"))
            .productImgs(null)
            .boardDetailImages(null)
            .products(List.of(createProductRequest()))
            .boardDetailRequest(new BoardDetailRequest("<p>상세설명입니다</p>"))
            .productInfoNoticeRequest(createProductInfoNoticeRequest())
            .build();
    }

    private MockMultipartFile createMockImage(String filename) {
        return new MockMultipartFile(
            "image",
            filename,
            "image/png",
            "fake image content".getBytes()
        );
    }

    private ProductRequest createProductRequest() {
        return new ProductRequest(
            "BREAD",
            "글루텐프리 식빵",
            new DietaryTagsRequest(true, false, true, false, false),
            0,
            100,
            new AvailabilityRequest(true, true, true, true, true, false, false),
            new NutritionInfoRequest(300, 50, 30, 5, 10, 8, 200)
        );
    }

    private ProductInfoNoticeRequest createProductInfoNoticeRequest() {
        return new ProductInfoNoticeRequest(
            "글루텐프리 빵 세트",
            "빵류",
            "빵그리의 오븐",
            "서울특별시",
            "제조일자 별도 표기",
            "제조일로부터 5일",
            "냉동보관",
            "1세트",
            "쌀가루, 설탕, 버터",
            "별도 표기",
            "해당없음",
            "알레르기 주의",
            "해당없음"
        );
    }
}
