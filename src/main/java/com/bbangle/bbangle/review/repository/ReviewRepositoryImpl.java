package com.bbangle.bbangle.review.repository;

import com.bbangle.bbangle.review.domain.QReview;
import com.bbangle.bbangle.review.dto.ReviewDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewQueryDSLRepository {

    private final JPAQueryFactory queryFactory;
    private static final QReview review = QReview.review;

    @Override
    public List<ReviewDto> findByBoardId(Long boardId) {
        return queryFactory.select(
                Projections.constructor(
                    ReviewDto.class,
                    review.badgeTaste,
                    review.badgeBrix,
                    review.badgeTexture,
                    review.rate
                )
            ).from(review)
            .where(review.boardId.eq(boardId))
            .fetch();
    }
}
