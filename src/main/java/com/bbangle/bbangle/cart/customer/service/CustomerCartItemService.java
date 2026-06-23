package com.bbangle.bbangle.cart.customer.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.cart.domain.Cart;
import com.bbangle.bbangle.cart.domain.CartItem;
import com.bbangle.bbangle.cart.repository.CartItemRepository;
import com.bbangle.bbangle.member.domain.Member;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerCartItemService {

    private final CartItemRepository cartItemRepository;

    @Transactional(readOnly = true)
    public Optional<CartItem> findCartItem(Cart cart, Board item) {
        return cartItemRepository.findByCartAndItem(cart, item);
    }

    @Transactional(readOnly = true)
    public List<CartItem> findAllByMember(Member member) {
        return cartItemRepository.findCartItemsByMember(member);
    }

    @Transactional
    public CartItem createCartItem(Cart cart, Board item) {
        return cartItemRepository.save(CartItem.create(cart, item));
    }
}
