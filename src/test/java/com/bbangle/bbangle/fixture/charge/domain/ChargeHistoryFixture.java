package com.bbangle.bbangle.fixture.charge.domain;

import com.bbangle.bbangle.charge.domain.ChargeHistory;
import com.bbangle.bbangle.charge.domain.enums.ChargeCategory;
import com.bbangle.bbangle.charge.domain.enums.ChargeReferenceType;
import com.bbangle.bbangle.charge.domain.enums.ChargeTransactionStatus;
import com.bbangle.bbangle.seller.domain.Seller;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.test.util.ReflectionTestUtils;

public final class ChargeHistoryFixture {

  private ChargeHistoryFixture() {
  }

  public static ChargeHistory createAccumulate(Seller seller, LocalDate baseDate) {
    return ChargeHistory.builder()
        .seller(seller)
        .category(ChargeCategory.ACCUMULATE)
        .amount(new BigDecimal("50000"))
        .balanceAfter(new BigDecimal("150000"))
        .status(ChargeTransactionStatus.COMPLETED)
        .baseDate(baseDate)
        .referenceType(ChargeReferenceType.SETTLEMENT)
        .referenceId(1L)
        .build();
  }

  public static ChargeHistory createDeduct(Seller seller, LocalDate baseDate) {
    return ChargeHistory.builder()
        .seller(seller)
        .category(ChargeCategory.DEDUCT)
        .amount(new BigDecimal("25000"))
        .balanceAfter(new BigDecimal("125000"))
        .status(ChargeTransactionStatus.COMPLETED)
        .baseDate(baseDate)
        .referenceType(ChargeReferenceType.WITHDRAWAL)
        .referenceId(1L)
        .build();
  }

  public static ChargeHistory createPending(Seller seller, LocalDate baseDate) {
    return ChargeHistory.builder()
        .seller(seller)
        .category(ChargeCategory.ACCUMULATE)
        .amount(new BigDecimal("100000"))
        .balanceAfter(new BigDecimal("200000"))
        .status(ChargeTransactionStatus.PENDING)
        .baseDate(baseDate)
        .referenceType(ChargeReferenceType.SETTLEMENT)
        .referenceId(2L)
        .build();
  }

  public static ChargeHistory withId(ChargeHistory history, Long id) {
    ReflectionTestUtils.setField(history, "id", id);
    return history;
  }
}
