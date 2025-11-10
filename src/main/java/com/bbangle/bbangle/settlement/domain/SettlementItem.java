package com.bbangle.bbangle.settlement.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.settlement.domain.model.SettlementStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Getter
@Table(name = "settlement_item")
public class SettlementItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "settlement_number", columnDefinition = "VARCHAR(50)")
    private String settlementNumber;

    @Column(name = "type",   columnDefinition = "VARCHAR(20)")
    private String type;

    @Column(name = "scheduled_amount", precision = 15, scale = 2)
    private BigDecimal scheduledAmount;

    @Column(name = "base_date")
    private LocalDate baseDate;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",  columnDefinition = "VARCHAR(20)")
    private SettlementStatus status;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_settlement_id")
    private DailySettlement dailySettlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

}
