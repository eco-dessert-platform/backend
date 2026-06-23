package com.bbangle.bbangle.cart.repository;

import com.bbangle.bbangle.cart.domain.CartItem;
import com.bbangle.bbangle.member.domain.Member;
import java.util.List;

public interface CartItemQueryDSLRepository {

    List<CartItem> findCartItemsByMember(Member member);
}
