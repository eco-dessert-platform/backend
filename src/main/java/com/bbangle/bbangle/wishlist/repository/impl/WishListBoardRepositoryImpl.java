package com.bbangle.bbangle.wishlist.repository.impl;

import com.bbangle.bbangle.analytics.dto.DateAndCountDto;
import com.bbangle.bbangle.analytics.dto.QDateAndCountDto;
import com.bbangle.bbangle.wishlist.repository.WishListBoardQueryDSLRepository;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static com.bbangle.bbangle.wishlist.domain.QWishListBoard.wishListBoard;

@Repository
@RequiredArgsConstructor
public class WishListBoardRepositoryImpl implements WishListBoardQueryDSLRepository {

    private final JPAQueryFactory queryFactory;


    @Override
    public List<DateAndCountDto> countWishlistCreatedBetweenPeriod(LocalDate startLocalDate, LocalDate endLocalDate) {
        DateTemplate<Date> createdAt = getDateCreatedAt();
        Date startDate = Date.valueOf(startLocalDate);
        Date endDate = Date.valueOf(endLocalDate);

        return queryFactory.select(new QDateAndCountDto(
                        createdAt, wishListBoard.id.count()
                ))
                .from(wishListBoard)
                .where(createdAt.between(startDate, endDate))
                .groupBy(createdAt)
                .orderBy(createdAt.asc())
                .fetch();
    }


    private  DateTemplate<Date> getDateCreatedAt() {
        return Expressions.dateTemplate(Date.class, "DATE({0})", wishListBoard.createdAt);
    }

}
