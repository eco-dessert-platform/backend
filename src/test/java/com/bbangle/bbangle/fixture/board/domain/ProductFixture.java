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

    public static Product withId(Product product, Long id) {
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
