package com.bbangle.bbangle.cart.repository;

import com.bbangle.bbangle.cart.domain.CartItem;
import com.bbangle.bbangle.cart.domain.CartOption;
import java.util.List;
import java.util.Optional;
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

    // TODO : Test
    @Query("""
        select co
        from CartOption co
            join fetch co.option
            join co.cartItem ci
            join ci.cart c
        where co.id = :cartOptionId and c.member.id = :memberId
        """)
    Optional<CartOption> findByIdAndMemberId(@Param("cartOptionId") Long cartOptionId, @Param("memberId") Long memberId);
}
