package com.bbangle.bbangle.cart.repository;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.cart.domain.Cart;
import com.bbangle.bbangle.cart.domain.CartItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends
    JpaRepository<CartItem, Long>, CartItemQueryDSLRepository {

    Optional<CartItem> findByCartAndItem(Cart cart, Board item);
}
