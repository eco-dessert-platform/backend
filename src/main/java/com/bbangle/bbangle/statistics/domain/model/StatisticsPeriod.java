package com.bbangle.bbangle.statistics.domain.model;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public enum StatisticsPeriod {
    DAY,
    WEEK,
    MONTH;

    public DateRange resolveDateRange(LocalDate targetDate, int bucketCount) {
        return switch (this) {
            case DAY -> new DateRange(targetDate.minusDays(bucketCount - 1L), targetDate);
            case WEEK -> new DateRange(
                targetDate.minusWeeks(bucketCount - 1L),
                targetDate.plusDays(6)
            );
            case MONTH -> new DateRange(
                targetDate.minusMonths(bucketCount - 1L).withDayOfMonth(1),
                targetDate.with(TemporalAdjusters.lastDayOfMonth())
            );
        };
    }

    public LocalDate nextBucketStart(LocalDate current) {
        return switch (this) {
            case DAY -> current.plusDays(1);
            case WEEK -> current.plusWeeks(1);
            case MONTH -> current.plusMonths(1).withDayOfMonth(1);
        };
    }

    public LocalDate resolveBucketEnd(LocalDate bucketStart) {
        return switch (this) {
            case DAY -> bucketStart;
            case WEEK -> bucketStart.plusDays(6);
            case MONTH -> bucketStart.with(TemporalAdjusters.lastDayOfMonth());
        };
    }

    public static StatisticsPeriod from(StatisticsPeriod period) {
        if (period == null) {
            throw new BbangleException(BbangleErrorCode.INVALID_STATISTICS_PERIOD);
        }
        return period;
    }
}
