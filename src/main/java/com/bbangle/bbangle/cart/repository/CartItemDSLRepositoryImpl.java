package com.bbangle.bbangle.cart.repository;

import static com.bbangle.bbangle.cart.domain.QCart.cart;
import static com.bbangle.bbangle.cart.domain.QCartItem.cartItem;
import static com.bbangle.bbangle.cart.domain.QCartOption.cartOption;

import com.bbangle.bbangle.cart.domain.CartItem;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CartItemDSLRepositoryImpl implements CartItemDSLRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CartItem> findAllWithOptionsByMemberIdAndOptionIds(Long memberId, List<Long> optionIds) {
        return queryFactory
            .selectFrom(cartItem)
            .distinct()
            .join(cartItem.options, cartOption)
            .join(cartItem.cart, cart)
            .where(
                cart.member.id.eq(memberId),
                cartOption.id.in(optionIds)
            )
            .fetch();
    }
}
