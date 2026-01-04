package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.store.domain.Store;
import java.util.ArrayList;

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
            .discountRate(0)
            .deliveryFee(0)
            .store(store)
            .products(new ArrayList<>())
            .productImgs(new ArrayList<>());
    }

    /* =====================
       기존 default 계열
     ===================== */
    public static Board defaultBoard() {
        return baseBuilder(StoreFixture.defaultStore(), "상품명")
            .build();
    }

    public static Board defaultBoard(String title) {
        return baseBuilder(StoreFixture.defaultStore(), title)
            .build();
    }

    public static Board defaultBoard(Store store, String title) {
        return baseBuilder(store, title)
            .build();
    }

    /**
     * 관리자 조회 대상 isCrawling = true isDeleted  = false
     */
    public static Board crawlingActiveBoard(Store store, String title) {
        return baseBuilder(store, title)
            .isCrawling(true)
            .isDeleted(false)
            .build();
    }

    /**
     * 크롤링 데이터이지만 삭제됨
     */
    public static Board crawlingDeletedBoard(Store store, String title) {
        return baseBuilder(store, title)
            .isCrawling(true)
            .isDeleted(true)
            .build();
    }

    /**
     * 수동 등록 상품 (관리자 조회 제외)
     */
    public static Board nonCrawlingActiveBoard(Store store, String title) {
        return baseBuilder(store, title)
            .isCrawling(false)
            .isDeleted(false)
            .build();
    }

    /**
     * 완전 제외 대상
     */
    public static Board nonCrawlingDeletedBoard(Store store, String title) {
        return baseBuilder(store, title)
            .isCrawling(false)
            .isDeleted(true)
            .build();
    }
}
