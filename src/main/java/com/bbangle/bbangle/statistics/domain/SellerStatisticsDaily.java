package com.bbangle.bbangle.statistics.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.seller.domain.Seller;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
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

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "total_orders")
    private Integer totalOrders;

    @Column(name = "total_buyers")
    private Integer totalBuyers;

    @Column(name = "refund_amount", precision = 15, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_count")
    private Integer refundCount;

    @Column(name = "refund_rate", precision = 5, scale = 2)
    private BigDecimal refundRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;

    @Builder(access = AccessLevel.PRIVATE)
    private SellerStatisticsDaily(
        LocalDateTime statDate,
        Integer weekday,
        BigDecimal totalAmount,
        Integer totalOrders,
        Integer totalBuyers,
        BigDecimal refundAmount,
        Integer refundCount,
        BigDecimal refundRate,
        Seller seller
    ) {
        this.statDate = statDate;
        this.weekday = weekday;
        this.totalAmount = totalAmount;
        this.totalOrders = totalOrders;
        this.totalBuyers = totalBuyers;
        this.refundAmount = refundAmount;
        this.refundCount = refundCount;
        this.refundRate = refundRate;
        this.seller = seller;
    }

    public static SellerStatisticsDaily create(
        LocalDateTime statDate,
        Integer weekday,
        BigDecimal totalAmount,
        Integer totalOrders,
        Integer totalBuyers,
        BigDecimal refundAmount,
        Integer refundCount,
        BigDecimal refundRate,
        Seller seller
    ) {
        return SellerStatisticsDaily.builder()
            .statDate(statDate)
            .weekday(weekday)
            .totalAmount(totalAmount)
            .totalOrders(totalOrders)
            .totalBuyers(totalBuyers)
            .refundAmount(refundAmount)
            .refundCount(refundCount)
            .refundRate(refundRate)
            .seller(seller)
            .build();
    }

    public long getTotalOrdersCount() {
        return totalOrders == null ? 0L : totalOrders.longValue();
    }

    public long getTotalBuyersCount() {
        return totalBuyers == null ? 0L : totalBuyers.longValue();
    }
}
