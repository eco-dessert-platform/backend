package com.bbangle.bbangle.statistics.domain;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Getter
@Table(name = "seller_statistics_daily")
public class SellerStatisticsDaily extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "stat_date")
    private LocalDateTime statDate;

    @Column(name = "weekday")
    private Integer weekday;

    @Column(name = "total_amount")
    private Long totalAmount;

    @Column(name = "total_orders")
    private Integer totalOrders;

    @Column(name = "total_buyers")
    private Integer totalBuyers;

    @Column(name = "refund_amount")
    private Long refundAmount;

    @Column(name = "refund_count")
    private Integer refundCount;

    @Column(name = "refund_rate", precision = 5, scale = 2)
    private BigDecimal refundRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Seller seller;
}
