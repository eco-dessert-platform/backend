package com.bbangle.bbangle.fixture.board.domain;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.BoardDetail;
import com.bbangle.bbangle.board.domain.DiscountType;
import com.bbangle.bbangle.board.domain.SaleStatus;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.store.domain.Store;
import java.util.ArrayList;
import org.springframework.test.util.ReflectionTestUtils;

public final class BoardFixture {

    private BoardFixture() {
    }

    /* =====================
       기본 공통 Builder
     ===================== */
    private static Board.BoardBuilder baseBuilder(Store store, String title) {
        return Board.builder()
            .title(title)
            .price(10_000)
            .discountType(DiscountType.RATE)
            .discountValue(0)
            .discountRate(0)
            .deliveryFee(0)
            .store(store)
            .saleStatus(SaleStatus.ON_SALE)
            .products(new ArrayList<>())
            .productImgs(new ArrayList<>());
    }

    /* =====================
       default 계열
     ===================== */
    public static Board defaultBoard() {
        return baseBuilder(StoreFixture.defaultStore(), "상품명")
            .build();
    }

    public static Board defaultBoard(String title) {
        return baseBuilder(StoreFixture.defaultStore(), title)
            .build();
    }

    public static Board defaultBoardWithStore(Store store, String title) {
        return baseBuilder(store, title)
            .build();
    }

    public static Board defaultBoardWithStoreAndDetail(Store store, BoardDetail detail, String title) {
        return baseBuilder(store, title)
            .boardDetail(detail)
            .build();
    }

    /* =====================
       crawling + store 조합
     ===================== */

    /**
     * 관리자 조회 대상 isCrawling = true, isDeleted = false
     */
    public static Board crawlingActiveBoardWithStore(Store store, String title) {
        Board board = baseBuilder(store, title).build();
        ReflectionTestUtils.setField(board, "isCrawling", true);
        ReflectionTestUtils.setField(board, "isDeleted", false);
        return board;
    }

    /**
     * 크롤링 데이터이지만 삭제됨 isCrawling = true, isDeleted = true
     */
    public static Board crawlingDeletedBoardWithStore(Store store, String title) {
        Board board = baseBuilder(store, title).build();
        ReflectionTestUtils.setField(board, "isCrawling", true);
        ReflectionTestUtils.setField(board, "isDeleted", true);
        return board;
    }

    /* =====================
       non-crawling + store 조합
     ===================== */

    /**
     * 수동 등록 상품 (관리자 조회 제외) isCrawling = false, isDeleted = false
     */
    public static Board nonCrawlingActiveBoardWithStore(Store store, String title) {
        Board board = baseBuilder(store, title).build();
        ReflectionTestUtils.setField(board, "isCrawling", false);
        ReflectionTestUtils.setField(board, "isDeleted", false);
        return board;
    }

    /**
     * 완전 제외 대상 isCrawling = false, isDeleted = true
     */
    public static Board nonCrawlingDeletedBoardWithStore(Store store, String title) {
        Board board = baseBuilder(store, title).build();
        ReflectionTestUtils.setField(board, "isCrawling", false);
        ReflectionTestUtils.setField(board, "isDeleted", true);
        return board;
    }

    /* =====================
       SaleStatus 관련 메서드
     ===================== */

    /**
     * 판매대기(PENDING) 상태 Board saleStatus = PENDING, isDeleted = false
     */
    public static Board pendingBoardWithStore(Store store, String title) {
        Board board = baseBuilder(store, title).build();
        ReflectionTestUtils.setField(board, "saleStatus", SaleStatus.PENDING);
        ReflectionTestUtils.setField(board, "isDeleted", false);
        return board;
    }

    /**
     * 품절(OUT_OF_STOCK) 상태 Board saleStatus = OUT_OF_STOCK, isDeleted = false
     */
    public static Board outOfStockBoardWithStore(Store store, String title) {
        Board board = baseBuilder(store, title).build();
        ReflectionTestUtils.setField(board, "saleStatus", SaleStatus.OUT_OF_STOCK);
        ReflectionTestUtils.setField(board, "isDeleted", false);
        return board;
    }

    /**
     * 판매중지(STOPPED) 상태 Board saleStatus = STOPPED, isDeleted = false
     */
    public static Board stoppedBoardWithStore(Store store, String title) {
        Board board = baseBuilder(store, title).build();
        ReflectionTestUtils.setField(board, "saleStatus", SaleStatus.STOPPED);
        ReflectionTestUtils.setField(board, "isDeleted", false);
        return board;
    }

    /**
     * 판매금지(BANNED) 상태 Board saleStatus = BANNED, isDeleted = false
     */
    public static Board bannedBoardWithStore(Store store, String title) {
        Board board = baseBuilder(store, title).build();
        ReflectionTestUtils.setField(board, "saleStatus", SaleStatus.BANNED);
        ReflectionTestUtils.setField(board, "isDeleted", false);
        return board;
    }
}
