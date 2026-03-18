package com.bbangle.bbangle.statistics.seller.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.statistics.domain.SellerStatisticsDaily;
import com.bbangle.bbangle.statistics.repository.SellerStatisticsRepository;
import com.bbangle.bbangle.statistics.seller.dto.DailyPaymentAmountResponse;
import com.bbangle.bbangle.statistics.seller.dto.DailyPaymentAmountResponse.DailyPaymentAmountItem;
import com.bbangle.bbangle.statistics.seller.dto.StatisticsPeriod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
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

    // 기준 날짜와 조회 단위에 맞춰 최근 7개 구간의 결제 통계를 조회한다.
    @Transactional(readOnly = true)
    public DailyPaymentAmountResponse getDailyPaymentAmount(
        Long sellerId,
        Optional<LocalDate> date,
        Optional<StatisticsPeriod> period
    ) {
        validateSeller(sellerId);
        StatisticsPeriod resolvedPeriod = period.orElse(StatisticsPeriod.DAY);
        LocalDate targetDate = date.orElse(LocalDate.now());
        DateRange range = resolveDateRange(targetDate, resolvedPeriod);

        List<SellerStatisticsDaily> rows =
            sellerStatisticsRepository.findBySellerIdAndStatDateBetweenOrderByStatDateAsc(
                sellerId,
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
                    .mapToLong(DailyPaymentAmountItem::getAmount)
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

    // 셀러 아이디 검증
    private void validateSeller(Long sellerId) {
        if (sellerId == null || !sellerRepository.existsById(sellerId)) {
            throw new BbangleException(BbangleErrorCode.SELLER_NOT_FOUND);
        }
    }

    // 일별 통계를 period 단위의 7개 버킷으로 묶어 응답 리스트를 만든다.
    private List<DailyPaymentAmountItem> buildItems(
        DateRange range,
        StatisticsPeriod period,
        Map<LocalDate, SellerStatisticsDaily> statisticsByDate
    ) {
        List<DailyPaymentAmountItem> items = new ArrayList<>();
        LocalDate cursor = range.startDate();

        for (int i = 0; i < BUCKET_COUNT; i++) {
            LocalDate bucketStart = cursor;
            LocalDate bucketEnd = resolveBucketEnd(bucketStart, period);

            long amount = bucketStart.datesUntil(bucketEnd.plusDays(1))
                .map(statisticsByDate::get)
                .filter(row -> row != null && row.getTotalAmount() != null)
                .map(SellerStatisticsDaily::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .longValue();

            items.add(new DailyPaymentAmountItem(bucketStart, amount));
            cursor = nextBucketStart(cursor, period);
        }

        return items;
    }

    // 기준 날짜를 포함하는 최근 7개 구간의 전체 조회 범위를 계산한다.
    private DateRange resolveDateRange(LocalDate targetDate, StatisticsPeriod period) {
        if (period == null) {
            throw new BbangleException(BbangleErrorCode.INVALID_OAUTH_PARAMS);
        }

        if (period == StatisticsPeriod.DAY) {
            return validateDateRange(
                targetDate.minusDays(BUCKET_COUNT - 1L),
                targetDate
            );
        }

        if (period == StatisticsPeriod.WEEK) {
            return validateDateRange(
                targetDate.minusWeeks(BUCKET_COUNT - 1L),
                targetDate.plusDays(6)
            );
        }

        if (period == StatisticsPeriod.MONTH) {
            return validateDateRange(
                targetDate.minusMonths(BUCKET_COUNT - 1L).withDayOfMonth(1),
                targetDate.with(TemporalAdjusters.lastDayOfMonth())
            );
        }

        throw new BbangleException(BbangleErrorCode.INVALID_OAUTH_PARAMS);
    }

    // 현재 구간 시작일에서 다음 구간 시작일로 이동한다.
    private LocalDate nextBucketStart(LocalDate current, StatisticsPeriod period) {
        if (period == StatisticsPeriod.DAY) {
            return current.plusDays(1);
        }
        if (period == StatisticsPeriod.WEEK) {
            return current.plusWeeks(1);
        }
        if (period == StatisticsPeriod.MONTH) {
            return current.plusMonths(1).withDayOfMonth(1);
        }
        throw new BbangleException(BbangleErrorCode.INVALID_OAUTH_PARAMS);
    }

    // 단일 버킷의 종료일을 period에 맞춰 계산한다.
    private LocalDate resolveBucketEnd(LocalDate bucketStart, StatisticsPeriod period) {
        if (period == StatisticsPeriod.DAY) {
            return bucketStart;
        }
        if (period == StatisticsPeriod.WEEK) {
            return bucketStart.plusDays(6);
        }
        if (period == StatisticsPeriod.MONTH) {
            return bucketStart.with(TemporalAdjusters.lastDayOfMonth());
        }
        throw new BbangleException(BbangleErrorCode.INVALID_OAUTH_PARAMS);
    }

    // 계산된 시작일과 종료일의 순서를 검증한 뒤 범위 객체로 반환한다.
    private DateRange validateDateRange(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new BbangleException(BbangleErrorCode.INVALID_OAUTH_PARAMS);
        }
        return new DateRange(start, end);
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {

    }
}
