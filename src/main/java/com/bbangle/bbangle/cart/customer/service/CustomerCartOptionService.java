package com.bbangle.bbangle.cart.customer.service;

import com.bbangle.bbangle.cart.domain.Cart;
import com.bbangle.bbangle.cart.domain.CartOption;
import com.bbangle.bbangle.cart.repository.CartOptionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerCartOptionService {

    private final CartOptionRepository cartOptionRepository;

    @Transactional(readOnly = true)
    public List<CartOption> findAllByCart(Cart cart) {
        return cartOptionRepository.findAllByCart(cart);
    }

    @Transactional
    public void updateQuantity(CartOption cartOption, int quantity) {
        cartOption.updateQuantity(quantity);
    }
}
