package com.bbangle.bbangle.cart.customer.service;

import com.bbangle.bbangle.cart.domain.Cart;
import com.bbangle.bbangle.cart.repository.CartRepository;
import com.bbangle.bbangle.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO : Test
@Service
@RequiredArgsConstructor
public class CustomerCartService {

    private final CartRepository cartRepository;

    @Transactional
    public Cart findByMemberOrCreateCart(Member member) {
        return cartRepository.findCartByMember(member).orElseGet(() -> cartRepository.save(Cart.create(member)));
    }
}
