package com.bbangle.bbangle.wishlist.repository.util;

import com.bbangle.bbangle.store.domain.QStore;
import com.bbangle.bbangle.wishlist.domain.QWishListStore;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * <p>
 * WishList를 사용하는 곳에선 필수적으로 쓰이고 On 절은 NotNull이기에 코드가 지저분해짐 따라서 재사용성을 높이기 위해 작성
 * <p/>
 *
 * @author : 윤예찬
 * @version : 1.1.0
 * @since : 2024.06.07
 */
@Component
public class WishListStoreFilter {

    private static final QWishListStore wishListStore = QWishListStore.wishListStore;
    private static final BooleanExpression NOT_EQUALS = Expressions.FALSE;

    public BooleanExpression equalMemberId(Long memberId) {
        return Objects.nonNull(memberId) ?
            wishListStore.member.id.eq(memberId) :
            NOT_EQUALS;
    }

    public BooleanExpression equalStoreId(QStore store) {
        return wishListStore.store.eq(store);
    }

}
