package com.bbangle.bbangle.cart.customer.service;

import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.cart.domain.CartItem;
import com.bbangle.bbangle.cart.domain.CartOption;
import com.bbangle.bbangle.cart.repository.CartOptionRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void changeOption(CartOption cartOption, Product newOption) {
        cartOption.changeOption(newOption);
    }

    @Transactional(readOnly = true)
    public List<CartOption> findAllByCartItemIds(List<Long> cartItemIds) {
        return cartOptionRepository.findCartOptionsByCartItemIds(cartItemIds);
    }

    @Transactional(readOnly = true)
    public CartOption findByIdAndMemberId(Long memberId, Long cartOptionId) {
        return cartOptionRepository.findByIdAndMemberId(cartOptionId, memberId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOT_FOUND_CART_OPTION));
    }
}
