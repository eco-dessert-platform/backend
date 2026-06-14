package com.bbangle.bbangle.cart.customer.service;

import com.bbangle.bbangle.cart.domain.Cart;
import com.bbangle.bbangle.cart.repository.CartRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerCartService {

    private final CartRepository cartRepository;

    @Transactional(readOnly = true)
    public Cart findCartByMember(Member member) {
        return cartRepository.findCartByMember(member)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOT_FOUND_CART));
    }

    @Transactional
    public void CreateCart(Member member) {
        cartRepository.save(Cart.create(member));
    }
}
