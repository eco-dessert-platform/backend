package com.bbangle.bbangle.cart.repository;

import com.bbangle.bbangle.cart.domain.CartItem;
import com.bbangle.bbangle.cart.domain.CartOption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartOptionRepository extends JpaRepository<CartOption, Long> {
    List<CartOption> findAllByCartItem(CartItem cartItem);
}
