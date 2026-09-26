package com.bbangle.bbangle.fixture.board.domain;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.store.domain.Store;
import org.springframework.test.util.ReflectionTestUtils;

public final class ProductFixture {
    private ProductFixture() {
    }

    public static Product create(Board board, String title) {
        return Product.builder()
            .title(title)
            .board(board)
            .build();
    }

    public static Product create(Board board, String title, int price) {
        return Product.builder()
            .title(title)
            .board(board)
            .price(price)
            .build();
    }

    /** 주문 가능한 옵션. 옵션 가격은 게시글 가격에 더해지는 '추가금'이다. */
    public static Product orderableOption(Board board, String title, int addedPrice, int stock) {
        return Product.builder()
            .board(board)
            .store(board.getStore())
            .title(title)
            .price(addedPrice)
            .stock(stock)
            .soldout(false)
            .build();
    }

    public static Product defaultProductWithStore(Store store) {
        return Product.builder()
            .store(store)
            .title("테스트 상품")
            .price(10000)
            .build();
    }

    public static Product createWithStock(Board board, String title, int stock) {
        return Product.builder()
            .title(title)
            .board(board)
            .stock(stock)
            .soldout(false)
            .build();
    }

    /**
     * 6개 태그(glutenFree ~ lowFat)를 직접 지정해 생성한다.
     * getTagEnums() 테스트처럼 태그 조합에 따른 반환값 검증에 사용한다.
     * board는 필요 없는 테스트이므로 세팅하지 않는다.
     */
    public static Product withTags(
        boolean glutenFree,
        boolean highProtein,
        boolean sugarFree,
        boolean vegan,
        boolean ketogenic,
        boolean lowFat
    ) {
        return Product.builder()
            .title("테스트 상품")
            .glutenFreeTag(glutenFree)
            .highProteinTag(highProtein)
            .sugarFreeTag(sugarFree)
            .veganTag(vegan)
            .ketogenicTag(ketogenic)
            .lowFatTag(lowFat)
            .build();
    }

    /**
     * 요일 검증(validate)을 통과할 수 있도록 월요일만 true로 설정해 생성한다.
     * Repository 슬라이스 테스트처럼 실제 DB에 저장 가능한 유효한 Product가 필요할 때 사용한다.
     */
    public static Product createValidWithBoardAndMonday(Board board, String title) {
        return new Product(
            board, title, 0, "BREAD", 10,
            false, false, false, false, false,
            true, false, false, false, false, false, false, // monday만 true
            null
        );
    }

    public static Product withId(Product product, Long id) {
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
