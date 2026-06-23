package com.bbangle.bbangle.cart.repository;

import static com.bbangle.bbangle.board.domain.QBoard.board;
import static com.bbangle.bbangle.cart.domain.QCart.cart;
import static com.bbangle.bbangle.cart.domain.QCartItem.cartItem;
import static com.bbangle.bbangle.store.domain.QStore.store;

import com.bbangle.bbangle.cart.domain.CartItem;
import com.bbangle.bbangle.member.domain.Member;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CartItemQueryDSLRepositoryImpl implements CartItemQueryDSLRepository {

    private final JPAQueryFactory queryFactory;

    // TODO : Test
    @Override
    public List<CartItem> findCartItemsByMember(Member member) {
        return queryFactory
            .selectDistinct(cartItem)
            .from(cartItem)
            .join(cartItem.cart, cart)
            .join(cartItem.item, board).fetchJoin()
            .join(board.store, store).fetchJoin()
            .where(cart.member.eq(member))
            .fetch();
    }
}
