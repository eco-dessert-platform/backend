package com.bbangle.bbangle.cart.customer.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.cart.domain.CartItem;
import com.bbangle.bbangle.cart.repository.CartItemRepository;
import com.bbangle.bbangle.member.domain.Member;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerCartItemService {

    private final CartItemRepository cartItemRepository;

    @Transactional(readOnly = true)
    public Optional<CartItem> findCartItem(Member member, Board item) {
        return cartItemRepository.findByMemberAndItem(member, item);
    }

    @Transactional
    public CartItem createCartItem(Member member, Board item) {
        return cartItemRepository.save(CartItem.create(member, item));
    }
}
