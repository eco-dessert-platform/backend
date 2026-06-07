package com.bbangle.bbangle.cart.customer.service;

import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.cart.domain.CartItem;
import com.bbangle.bbangle.cart.domain.CartOption;
import com.bbangle.bbangle.cart.repository.CartOptionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO : Test
@Service
@RequiredArgsConstructor
public class CustomerCartOptionService {

    private final CartOptionRepository cartOptionRepository;

    @Transactional
    public CartOption createCartOption(CartItem item, Product option, int quantity) {
        return cartOptionRepository.save(
            CartOption.create(item, option, quantity)
        );
    }

    @Transactional(readOnly = true)
    public List<CartOption> findAllByCartItem(CartItem cartItem) {
        return cartOptionRepository.findAllByCartItem(cartItem);
    }

    @Transactional
    public void updateQuantity(CartOption cartOption, int quantity) {
        cartOption.updateQuantity(quantity);
    }
}
