package com.bbangle.bbangle.fixture.charge.domain;

import com.bbangle.bbangle.charge.domain.ChargeBalance;
import com.bbangle.bbangle.seller.domain.Seller;
import java.math.BigDecimal;
import org.springframework.test.util.ReflectionTestUtils;

public final class ChargeBalanceFixture {

  private ChargeBalanceFixture() {
  }

  public static ChargeBalance createDefault(Seller seller) {
    return ChargeBalance.builder()
        .seller(seller)
        .balance(new BigDecimal("100000"))
        .totalDeposited(new BigDecimal("500000"))
        .totalWithdrawn(new BigDecimal("400000"))
        .build();
  }

  public static ChargeBalance createWithBalance(Seller seller, BigDecimal balance) {
    return ChargeBalance.builder()
        .seller(seller)
        .balance(balance)
        .totalDeposited(new BigDecimal("500000"))
        .totalWithdrawn(new BigDecimal("400000"))
        .build();
  }

  public static ChargeBalance withId(ChargeBalance chargeBalance, Long id) {
    ReflectionTestUtils.setField(chargeBalance, "id", id);
    return chargeBalance;
  }
}
