package com.bbangle.bbangle.cart.repository;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.cart.domain.CartItem;
import com.bbangle.bbangle.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByMemberAndItem(Member member, Board item);
}
