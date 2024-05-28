package com.bbangle.bbangle.review.repository;

import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

import static com.bbangle.bbangle.review.domain.QReview.review;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewQueryDSLRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Long countMembersUsingReview() {
        return queryFactory.select(review.memberId.countDistinct())
                .from(review)
                .fetchOne();
    }

    @Override
    public Long countReviewByPeriod(LocalDate startDate, LocalDate endDate) {
        DateTemplate<LocalDate> createdAt = Expressions.dateTemplate(LocalDate.class, "DATE({0})", review.createdAt);

        return queryFactory.select(review.id.count())
                .from(review)
                .where(createdAt.between(startDate, endDate))
                .fetchOne();
    }

}
