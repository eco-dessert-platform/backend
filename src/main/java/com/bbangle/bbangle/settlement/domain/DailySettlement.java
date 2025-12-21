package com.bbangle.bbangle.settlement.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.settlement.domain.model.SettlementMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "daily_settlement")
public class DailySettlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 정산예정일
    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    // 정산완료일
    @Column(name = "completed_date")
    private LocalDate completedDate;

    // 결제금액
    @Column(name= "amount" , precision = 15, scale = 2)
    private BigDecimal amount;

    // 수수료
    @Column(name= "fee" , precision = 15, scale = 2)
    private BigDecimal fee; // 수수료

    // 공제/환급
    @Column(name= "deductible_refund" , precision = 15, scale = 2)
    private BigDecimal deductibleRefund; // 공제/환급

    // 지급보류
    @Column(name= "with_holding_payment" , precision = 15, scale = 2)
    private BigDecimal withHoldingPayment;

    // 정산방식
    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_method", columnDefinition = "VARCHAR(20)")
    private SettlementMethod settlementMethod;


    // TODO: 금액을 다루는 컬럼의 경우 VO 객체로 관리하는 방법 고려

}
