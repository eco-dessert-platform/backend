//package com.bbangle.bbangle.board.service;
//
//import static com.bbangle.bbangle.fixturemonkey.FixtureMonkeyConfig.fixtureMonkey;
//import static org.assertj.core.api.Assertions.assertThat;
//
//import com.bbangle.bbangle.board.domain.Board;
//import com.bbangle.bbangle.board.domain.BoardDetail;
//import com.bbangle.bbangle.board.domain.Product;
//import com.bbangle.bbangle.board.domain.ProductInfoNotice;
//import com.bbangle.bbangle.board.dto.BoardDetailRequest;
//import com.bbangle.bbangle.board.dto.BoardUploadRequest;
//import com.bbangle.bbangle.board.dto.ProductInfoNoticeRequest;
//import com.bbangle.bbangle.board.dto.ProductRequest;
//import com.bbangle.bbangle.board.repository.BoardDetailRepository;
//import com.bbangle.bbangle.board.repository.BoardRepository;
//import com.bbangle.bbangle.board.repository.ProductInfoNoticeRepository;
//import com.bbangle.bbangle.board.repository.ProductRepository;
//import com.bbangle.bbangle.exception.BbangleErrorCode;
//import com.bbangle.bbangle.exception.BbangleException;
//import com.bbangle.bbangle.store.domain.Store;
//import com.bbangle.bbangle.store.repository.StoreRepository;
//import jakarta.transaction.Transactional;
//import java.util.List;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//
//@ActiveProfiles("test")
//@SpringBootTest
//@Transactional
//class BoardUploadServiceTest {
//
//    @Autowired
//    private BoardUploadService boardUploadService;
//
//    @Autowired
//    private BoardRepository boardRepository;
//
//    @Autowired
//    private StoreRepository storeRepository;
//
//    @Autowired
//    private BoardDetailRepository boardDetailRepository;
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    @Autowired
//    private ProductInfoNoticeRepository productInfoNoticeRepository;
//
//    private Store savedStore;
//    private BoardUploadRequest boardUploadRequest;
//
//    @BeforeEach
//    void setUp() {
//        // Given
//        savedStore = storeRepository.save(fixtureMonkey.giveMeOne(Store.class));
//
//        ProductInfoNoticeRequest productInfoNoticeRequest = fixtureMonkey.giveMeBuilder(ProductInfoNoticeRequest.class)
//                .set("productName", "이름은 3글자 이상이어야 합니다.")
//                .sample();
//
//        BoardDetailRequest boardDetailRequest = fixtureMonkey.giveMeBuilder(BoardDetailRequest.class)
//                .set("content", "<p>상세페이지 내용입니다.</p>")
//                .sample();
//
//        List<ProductRequest> productRequests = fixtureMonkey.giveMeBuilder(ProductRequest.class)
//                .set("title", "초코 크로와상")
//                .set("monday", true)
//                .sampleList(3);
//
//        boardUploadRequest = fixtureMonkey.giveMeBuilder(BoardUploadRequest.class)
//                .set("boardTitle", "신제품 출시")
//                .set("price", 10000)
//                .set("discountRate", 50)
//                .set("discountPrice", 0)
//                .set("deliveryFee", 2500)
//                .set("productRequests", productRequests)
//                .set("boardDetailRequest", boardDetailRequest)
//                .set("productInfoNoticeRequest", productInfoNoticeRequest)
//                .sample();
//    }
//
//    @Test
//    @DisplayName("Board 엔티티 저장 성공 테스트")
//    void saveBoardTest() {
//        // When
//        Long boardId = boardUploadService.upload(savedStore.getId(), boardUploadRequest);
//
//        // Then
//        Board savedBoard = boardRepository.findById(boardId)
//                .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND));
//
//        assertThat(savedBoard.getTitle()).isEqualTo(boardUploadRequest.getBoardTitle());
//        assertThat(savedBoard.getPrice()).isEqualTo(boardUploadRequest.getPrice());
//        assertThat(savedBoard.getDiscountRate()).isEqualTo(boardUploadRequest.getDiscountRate());
//        assertThat(savedBoard.getDeliveryFee()).isEqualTo(boardUploadRequest.getDeliveryFee());
//    }
//
//    @Test
//    @DisplayName("Board를 저장할 때 BoardDetail를 저장한다")
//    void saveBoardDetailTest() {
//        // When
//        Long boardId = boardUploadService.upload(savedStore.getId(), boardUploadRequest);
//
//        // Then
//        Board savedBoard = boardRepository.findById(boardId)
//                .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND));
//        BoardDetail boardDetail = savedBoard.getBoardDetail();
//
//        assertThat(boardDetail).isNotNull();
//        assertThat(boardDetail.getContent())
//                .isEqualTo(boardUploadRequest.getBoardDetailRequest().getContent());
//    }
//
//    @Test
//    @DisplayName("Board를 저장할 때 Products를 저장한다")
//    void saveProductsTest() {
//        // When
//        Long boardId = boardUploadService.upload(savedStore.getId(), boardUploadRequest);
//
//        // Then
//        Board savedBoard = boardRepository.findById(boardId)
//                .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND));
//        List<Product> products = savedBoard.getProducts();
//
//        assertThat(products).hasSize(boardUploadRequest.getProductRequests().size());
//        assertThat(products.get(0).getTitle())
//                .isEqualTo(boardUploadRequest.getProductRequests().get(0).getTitle());
//    }
//
//    @Test
//    @DisplayName("Board를 저장할 때 ProductInfoNotice를 저장한다")
//    void saveProductInfoNoticeTest() {
//        // When
//        Long boardId = boardUploadService.upload(savedStore.getId(), boardUploadRequest);
//
//        // Then
//        Board savedBoard = boardRepository.findById(boardId)
//                .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND));
//        ProductInfoNotice productInfoNotice = savedBoard.getProductInfoNotice();
//
//        assertThat(productInfoNotice).isNotNull();
//        assertThat(productInfoNotice.getProductName())
//                .isEqualTo(boardUploadRequest.getProductInfoNoticeRequest().getProductName());
//    }
//
//    @Test
//    @DisplayName("Board를 삭제할 때 연관 BoardDetail, Products를  삭제한다")
//    void deleteCascadeTest() {
//        // Given
//        Long boardId = boardUploadService.upload(savedStore.getId(), boardUploadRequest);
//
//        // When
//        boardRepository.deleteById(boardId);
//
//        // Then
//        assertThat(boardRepository.findById(boardId)).isEmpty();
//        assertThat(boardDetailRepository.findByBoardId(boardId)).isEmpty();
//        assertThat(productRepository.findByBoardId(boardId)).isEmpty();
//    }
//}
