package com.bbangle.bbangle.board.service;

import static com.bbangle.bbangle.fixturemonkey.FixtureMonkeyConfig.fixtureMonkey;
import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.dto.BoardDetailRequest;
import com.bbangle.bbangle.board.dto.BoardUploadRequest;
import com.bbangle.bbangle.board.dto.ProductInfoNoticeRequest;
import com.bbangle.bbangle.board.dto.ProductRequest;
import com.bbangle.bbangle.board.repository.BoardDetailRepository;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductImgRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class BoardUploadServiceTest {

    @Autowired
    private BoardUploadService boardUploadService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private BoardDetailRepository boardDetailRepository;

    @Autowired
    private ProductImgRepository productImgRepository;

    @Autowired
    private ProductImgService productImgService;

    @Test
    @DisplayName("게시판 업로드 성공")
    void uploadBoard_Success() {
        // Given
        Store store = storeRepository.save(fixtureMonkey.giveMeOne(Store.class));
        Long storeId = store.getId();

        ProductInfoNoticeRequest productInfoNoticeRequest = fixtureMonkey.giveMeBuilder(ProductInfoNoticeRequest.class)
                .set("productName", "이름은 3글자 이상이어야 합니다.")
                .sample();

        BoardDetailRequest boardDetailRequest = fixtureMonkey.giveMeBuilder(BoardDetailRequest.class)
                .set("content", "<p>상세페이지 내용입니다.</p>")
                .sample();

        List<ProductRequest> productRequests = fixtureMonkey.giveMeBuilder(ProductRequest.class)
                .set("title", "초코 크로와상")
                .set("monday", true)
                .sampleList(3);

        BoardUploadRequest boardUploadRequest = fixtureMonkey.giveMeBuilder(BoardUploadRequest.class)
                .set("boardTitle", "신제품 출시")
                .set("price", 10000)
                .set("discountRate", 50)
                .set("deliveryFee", 2500)
                .set("productRequests", productRequests)
                .set("boardDetail", boardDetailRequest)
                .set("productInfoNotice" ,productInfoNoticeRequest)
                .sample();

        // When
        boardUploadService.upload(storeId, boardUploadRequest);

        // Then
        List<Board> savedBoard = boardRepository.findAll();
        assertThat(savedBoard).isNotEmpty();
    }
}
