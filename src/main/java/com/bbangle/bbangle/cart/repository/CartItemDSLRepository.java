package com.bbangle.bbangle.cart.repository;

import com.bbangle.bbangle.cart.domain.CartItem;
import java.util.List;

public interface CartItemDSLRepository {

    List<CartItem> findAllWithOptionsByMemberIdAndOptionIds(Long memberId, List<Long> optionIds);
}
