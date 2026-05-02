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
import com.bbangle.bbangle.statistics.seller.dto.DailyPaymentCountResponse;
import com.bbangle.bbangle.statistics.seller.dto.DailyPaymentCountResponse.DailyPaymentCountItem;
import com.bbangle.bbangle.statistics.seller.dto.WeekdayPaymentAmountResponse;
import com.bbangle.bbangle.statistics.seller.dto.WeekdayPaymentAmountResponse.WeekdayPaymentAmountItem;
import java.math.BigDecimal;
import java.time.DayOfWeek;
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

    // 통계 응답은 항상 7개 버킷(일/주/월)으로 고정합니다.
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

        // 같은 날짜의 통계는 하나만 사용하도록 date 기준 맵으로 변환합니다.
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

    @Transactional(readOnly = true)
    public WeekdayPaymentAmountResponse getWeekdayPaymentAmount(
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

        return new WeekdayPaymentAmountResponse(
            range.startDate(),
            range.endDate(),
            resolvedPeriod,
            buildWeekdayItems(range, statisticsByDate)
        );
    }

    @Transactional(readOnly = true)
    public DailyPaymentCountResponse getDailyPaymentCount(
        Long sellerId,
        Optional<LocalDate> date,
        Optional<StatisticsPeriod> period
    ) {
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.SELLER_NOT_FOUND));

        StatisticsPeriod resolvedPeriod = StatisticsPeriod.from(period.orElse(StatisticsPeriod.DAY));
        LocalDate targetDate = date.orElse(LocalDate.now());
        DateRange range = resolvedPeriod.resolveDateRange(targetDate, BUCKET_COUNT);

        // 조회 구간의 일별 통계만 가져와 이후 버킷 단위로 다시 합산합니다.
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

        List<DailyPaymentCountItem> items = buildCountItems(range, resolvedPeriod, statisticsByDate);
        Long averageBuyerCount = resolvedPeriod == StatisticsPeriod.DAY
            ? null
            : Math.round(
                items.stream()
                    .mapToLong(DailyPaymentCountItem::buyerCount)
                    .average()
                    .orElse(0)
            );
        Long averagePaymentCount = resolvedPeriod == StatisticsPeriod.DAY
            ? null
            : Math.round(
                items.stream()
                    .mapToLong(DailyPaymentCountItem::paymentCount)
                    .average()
                    .orElse(0)
            );

        return new DailyPaymentCountResponse(
            range.startDate(),
            range.endDate(),
            resolvedPeriod,
            averageBuyerCount,
            averagePaymentCount,
            items
        );
    }

    private List<DailyPaymentAmountItem> buildItems(
        DateRange range,
        StatisticsPeriod period,
        Map<LocalDate, SellerStatisticsDaily> statisticsByDate
    ) {
        // 각 버킷에 포함되는 날짜들의 매출 합계를 계산합니다.
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

    private List<DailyPaymentCountItem> buildCountItems(
        DateRange range,
        StatisticsPeriod period,
        Map<LocalDate, SellerStatisticsDaily> statisticsByDate
    ) {
        // 구매자 수와 주문 수를 같은 버킷 기준으로 각각 누적합니다.
        List<DailyPaymentCountItem> items = new ArrayList<>();
        LocalDate cursor = range.startDate();

        for (int i = 0; i < BUCKET_COUNT; i++) {
            LocalDate bucketStart = cursor;
            LocalDate bucketEnd = period.resolveBucketEnd(bucketStart);

            long buyerCount = bucketStart.datesUntil(bucketEnd.plusDays(1))
                .map(statisticsByDate::get)
                .filter(row -> row != null)
                .mapToLong(SellerStatisticsDaily::getTotalBuyersCount)
                .sum();

            long paymentCount = bucketStart.datesUntil(bucketEnd.plusDays(1))
                .map(statisticsByDate::get)
                .filter(row -> row != null)
                .mapToLong(SellerStatisticsDaily::getTotalOrdersCount)
                .sum();

            items.add(new DailyPaymentCountItem(bucketStart, buyerCount, paymentCount));
            cursor = period.nextBucketStart(cursor);
        }

        return items;
    }

    private List<WeekdayPaymentAmountItem> buildWeekdayItems(
        DateRange range,
        Map<LocalDate, SellerStatisticsDaily> statisticsByDate
    ) {
        List<WeekdayPaymentAmountItem> items = new ArrayList<>();

        for (int weekday = DayOfWeek.MONDAY.getValue(); weekday <= DayOfWeek.SUNDAY.getValue(); weekday++) {
            final int currentWeekday = weekday;
            List<LocalDate> matchingDates = range.startDate()
                .datesUntil(range.endDate().plusDays(1))
                .filter(date -> date.getDayOfWeek().getValue() == currentWeekday)
                .toList();

            long amount = matchingDates.stream()
                .map(statisticsByDate::get)
                .filter(row -> row != null && row.getTotalAmount() != null)
                .map(SellerStatisticsDaily::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .longValue();

            long averageAmount = matchingDates.isEmpty()
                ? 0L
                : Math.round((double) amount / matchingDates.size());

            items.add(new WeekdayPaymentAmountItem(currentWeekday, amount, averageAmount));
        }

        return items;
    }
}
