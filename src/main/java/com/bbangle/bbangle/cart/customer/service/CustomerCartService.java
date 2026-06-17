package com.bbangle.bbangle.cart.customer.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.cart.domain.Cart;
import com.bbangle.bbangle.cart.repository.CartRepository;
import com.bbangle.bbangle.member.domain.Member;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerCartService {

    private final CartRepository cartRepository;

    @Transactional(readOnly = true)
    public Optional<Cart> findCartByMemberAndBoard(Member member, Board item) {
        return cartRepository.findByMemberAndBoard(member, item);
    }

    @Transactional
    public Cart createCart(Member member, Board item) {
        return cartRepository.save(Cart.create(member, item));
    }
}
