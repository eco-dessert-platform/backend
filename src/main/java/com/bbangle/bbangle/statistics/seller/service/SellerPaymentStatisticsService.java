package com.bbangle.bbangle.statistics.seller.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.statistics.domain.SellerStatisticsDaily;
import com.bbangle.bbangle.statistics.domain.model.DateRange;
import com.bbangle.bbangle.statistics.domain.model.StatisticsPeriod;
import com.bbangle.bbangle.statistics.repository.SellerStatisticsRepository;
import com.bbangle.bbangle.statistics.seller.dto.DailyPaymentAmountResponse;
import com.bbangle.bbangle.statistics.seller.dto.DailyPaymentAmountResponse.DailyPaymentAmountItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerPaymentStatisticsService {

    private static final int BUCKET_COUNT = 7;

    private final SellerRepository sellerRepository;
    private final SellerStatisticsRepository sellerStatisticsRepository;

    @Transactional(readOnly = true)
    public DailyPaymentAmountResponse getDailyPaymentAmount(
        Long sellerId,
        Optional<LocalDate> date,
        Optional<StatisticsPeriod> period
    ) {
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.SELLER_NOT_FOUND));

        StatisticsPeriod resolvedPeriod = StatisticsPeriod.from(period.orElse(StatisticsPeriod.DAY));
        LocalDate targetDate = date.orElse(LocalDate.now());
        DateRange range = resolvedPeriod.resolveDateRange(targetDate, BUCKET_COUNT);

        List<SellerStatisticsDaily> rows =
            sellerStatisticsRepository.findBySellerIdAndStatDateBetweenOrderByStatDateAsc(
                seller.getId(),
                range.startDate().atStartOfDay(),
                range.endDate().atTime(LocalTime.MAX)
            );

        Map<LocalDate, SellerStatisticsDaily> statisticsByDate = rows.stream()
            .collect(Collectors.toMap(
                row -> row.getStatDate().toLocalDate(),
                Function.identity(),
                (first, second) -> first
            ));

        List<DailyPaymentAmountItem> items = buildItems(range, resolvedPeriod, statisticsByDate);
        Long averageAmount = resolvedPeriod == StatisticsPeriod.DAY
            ? null
            : Math.round(
                items.stream()
                    .mapToLong(DailyPaymentAmountItem::amount)
                    .average()
                    .orElse(0)
            );

        return new DailyPaymentAmountResponse(
            range.startDate(),
            range.endDate(),
            resolvedPeriod,
            averageAmount,
            items
        );
    }

    private List<DailyPaymentAmountItem> buildItems(
        DateRange range,
        StatisticsPeriod period,
        Map<LocalDate, SellerStatisticsDaily> statisticsByDate
    ) {
        List<DailyPaymentAmountItem> items = new ArrayList<>();
        LocalDate cursor = range.startDate();

        for (int i = 0; i < BUCKET_COUNT; i++) {
            LocalDate bucketStart = cursor;
            LocalDate bucketEnd = period.resolveBucketEnd(bucketStart);

            long amount = bucketStart.datesUntil(bucketEnd.plusDays(1))
                .map(statisticsByDate::get)
                .filter(row -> row != null && row.getTotalAmount() != null)
                .map(SellerStatisticsDaily::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .longValue();

            items.add(new DailyPaymentAmountItem(bucketStart, amount));
            cursor = period.nextBucketStart(cursor);
        }

        return items;
    }
}
