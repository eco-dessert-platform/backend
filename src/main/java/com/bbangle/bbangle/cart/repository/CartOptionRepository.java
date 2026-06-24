package com.bbangle.bbangle.cart.repository;

import com.bbangle.bbangle.cart.domain.CartItem;
import com.bbangle.bbangle.cart.domain.CartOption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartOptionRepository extends JpaRepository<CartOption, Long> {

    List<CartOption> findAllByCartItem(CartItem cartItem);

    @Query("""
            SELECT co
            FROM CartOption co
            JOIN FETCH co.option
            WHERE co.cartItem.id IN :cartItemIds
        """)
    List<CartOption> findCartOptionsByCartItemIds(@Param("cartItemIds") List<Long> cartItemIds);
}
