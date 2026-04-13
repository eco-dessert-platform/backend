package com.bbangle.bbangle.charge.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.seller.domain.Seller;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "charge_balance")
@Entity
public class ChargeBalance extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Comment("판매자 ID")
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id", unique = true, nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  private Seller seller;

  @Comment("현재 충전금 잔액")
  @Column(columnDefinition = "DECIMAL(15,2) DEFAULT 0", nullable = false)
  private BigDecimal balance;

  @Comment("누적 입금액")
  @Column(columnDefinition = "DECIMAL(15,2) DEFAULT 0", nullable = false)
  private BigDecimal totalDeposited;

  @Comment("누적 출금액")
  @Column(columnDefinition = "DECIMAL(15,2) DEFAULT 0", nullable = false)
  private BigDecimal totalWithdrawn;

  @Builder
  private ChargeBalance(Seller seller, BigDecimal balance, BigDecimal totalDeposited,
      BigDecimal totalWithdrawn) {
    this.seller = seller;
    this.balance = balance != null ? balance : BigDecimal.ZERO;
    this.totalDeposited = totalDeposited != null ? totalDeposited : BigDecimal.ZERO;
    this.totalWithdrawn = totalWithdrawn != null ? totalWithdrawn : BigDecimal.ZERO;
  }

  /**
   * 판매자별 충전금 생성
   */
  public static ChargeBalance create(Seller seller) {
    return ChargeBalance.builder()
        .seller(seller)
        .balance(BigDecimal.ZERO)
        .totalDeposited(BigDecimal.ZERO)
        .totalWithdrawn(BigDecimal.ZERO)
        .build();
  }

  /**
   * 충전금 입금 처리
   */
  public void deposit(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("입금 금액은 0보다 커야 합니다.");
    }
    this.balance = this.balance.add(amount);
    this.totalDeposited = this.totalDeposited.add(amount);
  }

  /**
   * 충전금 출금 처리
   */
  public void withdraw(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("출금 금액은 0보다 커야 합니다.");
    }
    if (this.balance.compareTo(amount) < 0) {
      throw new IllegalArgumentException("충전금이 부족합니다.");
    }
    this.balance = this.balance.subtract(amount);
    this.totalWithdrawn = this.totalWithdrawn.add(amount);
  }

  /**
   * 충전금 차감 처리
   */
  public void deduct(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("차감 금액은 0보다 커야 합니다.");
    }
    if (this.balance.compareTo(amount) < 0) {
      throw new IllegalArgumentException("충전금이 부족합니다.");
    }
    this.balance = this.balance.subtract(amount);
  }

  /**
   * 충전금이 충분한지 확인
   */
  public boolean isSufficientBalance(BigDecimal amount) {
    return this.balance.compareTo(amount) >= 0;
  }
}
