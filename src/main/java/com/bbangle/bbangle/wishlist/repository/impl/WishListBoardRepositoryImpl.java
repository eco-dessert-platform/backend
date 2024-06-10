package com.bbangle.bbangle.wishlist.repository.impl;

import com.bbangle.bbangle.analytics.dto.AnalyticsCreatedWithinPeriodResponseDto;
import com.bbangle.bbangle.analytics.dto.DateAndCountDto;
import com.bbangle.bbangle.wishlist.repository.WishListBoardQueryDSLRepository;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static com.bbangle.bbangle.wishlist.domain.QWishListBoard.wishListBoard;

@Repository
@RequiredArgsConstructor
public class WishListBoardRepositoryImpl implements WishListBoardQueryDSLRepository {

    private final JPAQueryFactory queryFactory;


    @Override
    public AnalyticsCreatedWithinPeriodResponseDto countWishlistCreatedBetweenPeriod(LocalDate startLocalDate, LocalDate endLocalDate) {
        DateTemplate<Date> createdAt = getDateCreatedAt();
        List<DateAndCountDto> dateAndCount = new ArrayList<>();
        Long total = 0L;
        Double daysBetween = calculateDaysBetween(startLocalDate, endLocalDate);

        for (LocalDate localDate = startLocalDate; !localDate.isAfter(endLocalDate); localDate = localDate.plusDays(1)) {
            Date date = Date.valueOf(localDate);

            Long count = queryFactory.select(wishListBoard.id.count())
                    .from(wishListBoard)
                    .where(createdAt.eq(date))
                    .fetchOne();

            dateAndCount.add(new DateAndCountDto(date, count));
            total += count;
        }

        Double rawAverage = (total / daysBetween);
        String average = String.format("%.2f", rawAverage);

        return new AnalyticsCreatedWithinPeriodResponseDto(dateAndCount, total, average);
    }


    private  DateTemplate<Date> getDateCreatedAt() {
        return Expressions.dateTemplate(Date.class, "DATE({0})", wishListBoard.createdAt);
    }


    public Double calculateDaysBetween(LocalDate startDate, LocalDate endDate) {
        return (double) (ChronoUnit.DAYS.between(startDate, endDate) + 1);
    }

}
