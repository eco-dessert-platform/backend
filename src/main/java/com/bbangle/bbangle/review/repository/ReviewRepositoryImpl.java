package com.bbangle.bbangle.review.repository;

import static java.util.stream.Collectors.toMap;

import com.bbangle.bbangle.analytics.admin.dto.AnalyticsCumulationResponseDto;
import com.bbangle.bbangle.analytics.admin.dto.DateAndCountDto;
import com.bbangle.bbangle.analytics.admin.dto.QAnalyticsCumulationResponseDto;
import com.bbangle.bbangle.analytics.admin.dto.QDateAndCountDto;
import com.bbangle.bbangle.board.customer.dto.AiLearningReviewDto;
import com.bbangle.bbangle.board.customer.dto.QAiLearningReviewDto;
import com.bbangle.bbangle.boardstatistic.customer.ranking.BoardGrade;
import com.bbangle.bbangle.image.domain.QImage;
import com.bbangle.bbangle.image.dto.ImageDto;
import com.bbangle.bbangle.image.dto.QImageDto;
import com.bbangle.bbangle.member.domain.QMember;
import com.bbangle.bbangle.review.dao.QReviewStatisticDao;
import com.bbangle.bbangle.review.dao.ReviewStatisticDao;
import com.bbangle.bbangle.review.domain.QReview;
import com.bbangle.bbangle.review.domain.QReviewLike;
import com.bbangle.bbangle.review.domain.ReviewCursor;
import com.bbangle.bbangle.review.domain.ReviewLike;
import com.bbangle.bbangle.review.dto.LikeCountPerReviewIdDto;
import com.bbangle.bbangle.review.dto.QLikeCountPerReviewIdDto;
import com.bbangle.bbangle.review.dto.QReviewCountPerBoardIdDto;
import com.bbangle.bbangle.review.dto.QReviewDto;
import com.bbangle.bbangle.review.dto.QReviewSingleDto;
import com.bbangle.bbangle.review.dto.ReviewBadgeDto;
import com.bbangle.bbangle.review.dto.ReviewCountPerBoardIdDto;
import com.bbangle.bbangle.review.dto.ReviewDto;
import com.bbangle.bbangle.review.dto.ReviewSingleDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewQueryDSLRepository {

    private static final QReview review = QReview.review;
    private static final QMember member = QMember.member;
    private static final QImage image = QImage.image;
    private static final QReviewLike reviewLike = QReviewLike.reviewLike;
    private static final Long PAGE_SIZE = 10L;

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;


    private JPAQuery<ReviewSingleDto> getPureReviewSingleDto() {
        return queryFactory
            .select(
                new QReviewSingleDto(
                    review.id,
                    review.memberId,
                    member.nickname,
                    review.rate,
                    review.badgeTaste,
                    review.badgeBrix,
                    review.badgeTexture,
                    review.content,
                    review.createdAt,
                    review.isBest,
                    review.boardId
                )
            )
            .from(review)
            .leftJoin(member)
            .on(review.memberId.eq(member.id));
    }

    @Override
    public List<ReviewSingleDto> getReviewSingleList(Long boardId, Long cursorId) {
        return getPureReviewSingleDto()
            .where(eqBoardId(boardId).and(getCursorCondition(cursorId)))
            .orderBy(review.id.desc())
            .limit(PAGE_SIZE + 1)
            .fetch();
    }

    @Override
    public Map<Long, List<ImageDto>> getImageMap(ReviewCursor reviewCursor) {
        BooleanBuilder imageCondition = getImageCondition(reviewCursor);
        List<Tuple> reviewImages = queryFactory.select(
                image.domainId,
                image.id,
                image.path
            )
            .from(image)
            .where(imageCondition)
            .fetch();
        return createImageMap(reviewImages);
    }

    @Override
    public List<ReviewLike> getLikeList(ReviewCursor reviewCursor) {
        BooleanBuilder likeCondition = getLikeCondition(reviewCursor);
        return queryFactory.selectFrom(reviewLike)
            .where(likeCondition)
            .fetch();
    }

    private BooleanBuilder getCursorCondition(Long cursorId) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(notDeleted());
        if (Objects.isNull(cursorId)) {
            return booleanBuilder;
        }

        booleanBuilder.and(review.id.loe(cursorId));
        return booleanBuilder;
    }

    @Override
    public ReviewSingleDto getReviewDetail(Long reviewId) {
        return getPureReviewSingleDto()
            .where(eqId(reviewId).and(notDeleted()))
            .orderBy(review.createdAt.desc())
            .fetchFirst();
    }

    @Override
    public List<ReviewSingleDto> getMyReviews(Long memberId, Long cursorId) {
        return getPureReviewSingleDto()
            .where(eqMemberId(memberId).and(getCursorCondition(cursorId)))
            .orderBy(review.createdAt.desc())
            .limit(PAGE_SIZE + 1)
            .fetch();
    }

    @Override
    public List<ImageDto> getAllImagesByBoardId(Long boardId, Long requestCursor) {
        List<Long> fetch = queryFactory
            .select(review.id)
            .from(review)
            .where(eqBoardId(boardId))
            .fetch();
        BooleanBuilder imageCondition = new BooleanBuilder();
        if (requestCursor != null) {
            imageCondition.and(image.id.loe(requestCursor));
        }
        imageCondition.and(image.domainId.in(fetch));
        return queryFactory
            .select(new QImageDto(
                image.id,
                image.path
            ))
            .from(image)
            .where(imageCondition)
            .orderBy(image.id.desc())
            .limit(PAGE_SIZE + 1)
            .fetch();
    }

    @Override
    public List<ReviewCountPerBoardIdDto> getReviewCount() {
        return queryFactory
            .select(
                new QReviewCountPerBoardIdDto(
                    review.boardId,
                    review.id.count()
                )
            )
            .from(review)
            .groupBy(review.boardId)
            .fetch();
    }

    @Override
    public List<LikeCountPerReviewIdDto> getLikeCount(Long minimumBestReviewStandard) {
        return queryFactory
            .select(
                new QLikeCountPerReviewIdDto(
                    reviewLike.reviewId,
                    reviewLike.memberId.count()
                )
            )
            .from(reviewLike)
            .groupBy(reviewLike.reviewId)
            .having(reviewLike.memberId.count()
                .goe(minimumBestReviewStandard))
            .orderBy(reviewLike.memberId.count()
                .desc())
            .fetch();
    }

    @Override
    public Map<Long, List<Long>> getBestReview(List<Long> reviewIds) {
        List<Tuple> fetch = queryFactory
            .select(
                review.boardId,
                review.id
            )
            .from(review)
            .where(review.id.in(reviewIds))
            .fetch();
        return fetch.stream()
            .collect(toMap(
                tuple -> tuple.get(review.boardId),
                tuple -> {
                    List<Long> reviewIdList = new ArrayList<>();
                    reviewIdList.add(tuple.get(review.id));
                    return reviewIdList;
                },
                (existList, newList) -> {
                    existList.addAll(newList);
                    return existList;
                }

            ));
    }

    @Override
    public void updateBestReview(List<Long> bestReviewIds) {
        queryFactory
            .update(review)
            .set(review.isBest, true)
            .where(review.id.in(bestReviewIds))
            .execute();

        em.flush();
        em.clear();
    }

    @Override
    public List<DateAndCountDto> countReviewCreatedBetweenPeriod(
        LocalDate startLocalDate,
        LocalDate endLocalDate
    ) {
        DateTemplate<Date> createdAt = getDateCreatedAt();
        Date startDate = Date.valueOf(startLocalDate);
        Date endDate = Date.valueOf(endLocalDate);

        return queryFactory.select(new QDateAndCountDto(
                createdAt, review.id.count()
            ))
            .from(review)
            .where(createdAt.between(startDate, endDate))
            .groupBy(createdAt)
            .orderBy(createdAt.asc())
            .fetch();
    }

    @Override
    public List<AnalyticsCumulationResponseDto> countCumulatedReviewBeforeEndDate(
        LocalDate startLocalDate,
        LocalDate endLocalDate
    ) {
        DateTemplate<Date> createdAt = getDateCreatedAt();
        Date endDate = Date.valueOf(endLocalDate);

        return queryFactory.select(new QAnalyticsCumulationResponseDto(
                createdAt, review.id.count()
            ))
            .from(review)
            .where(createdAt.loe(endDate))
            .groupBy(createdAt)
            .orderBy(createdAt.asc())
            .fetch();
    }

    @Override
    public List<BoardGrade> groupByBoardIdAndGetReviewCountAndReviewRate() {
        return queryFactory.select(review.boardId, review.id.count(), review.rate.avg())
            .from(review)
            .groupBy(review.boardId)
            .orderBy(review.boardId.asc())
            .fetch()
            .stream()
            .map(tuple -> BoardGrade.builder()
                .boardId(tuple.get(review.boardId))
                .count(tuple.get(review.id.count())
                    .intValue())
                .grade(BigDecimal.valueOf(tuple.get(review.rate.avg())))
                .build())
            .toList();
    }

    private BooleanBuilder getImageCondition(ReviewCursor reviewCursor) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (reviewCursor.reviewId() != null) {
            return booleanBuilder.and(image.domainId.eq(reviewCursor.reviewId()));
        }
        booleanBuilder.and(
            image.domainId.between(reviewCursor.nextCursor(), reviewCursor.lastCursor()));

        return booleanBuilder;
    }

    private Map<Long, List<ImageDto>> createImageMap(List<Tuple> reviewImages) {
        return reviewImages
            .stream()
            .collect(toMap(
                reviewImage -> reviewImage.get(image.domainId),
                reviewImage -> {
                    List<ImageDto> images = new ArrayList<>();
                    images.add(ImageDto.builder()
                        .id(reviewImage.get(image.id))
                        .url(reviewImage.get(image.path))
                        .build());
                    return images;
                },
                (existImages, newImage) -> {
                    existImages.addAll(newImage);
                    return existImages;
                }
            ));
    }

    private BooleanBuilder getLikeCondition(ReviewCursor reviewCursor) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (reviewCursor.reviewId() != null) {
            return booleanBuilder.and(reviewLike.reviewId.eq(reviewCursor.reviewId()));
        }
        return booleanBuilder.and(
            reviewLike.reviewId.between(reviewCursor.nextCursor(), reviewCursor.lastCursor()));
    }

    private BooleanExpression eqId(Long reviewId) {
        return review.id.eq(reviewId);
    }

    private BooleanExpression eqMemberId(Long memberId) {
        return review.memberId.eq(memberId);
    }

    private BooleanExpression notDeleted() {
        return review.isDeleted.eq(false);
    }

    private BooleanExpression eqBoardId(Long boardId) {
        return review.boardId.eq(boardId);
    }

    private DateTemplate<Date> getDateCreatedAt() {
        return Expressions.dateTemplate(Date.class, "DATE({0})", review.createdAt);
    }

    @Override
    public List<ReviewDto> findByBoardId(Long boardId) {
        return queryFactory.select(
                new QReviewDto(
                    review.badgeTaste,
                    review.badgeBrix,
                    review.badgeTexture,
                    review.rate
                )
            )
            .from(review)
            .where(review.boardId.eq(boardId).and(notDeleted()))
            .fetch();
    }

    @Override
    public List<ReviewBadgeDto> findReviewBadgeByBoardId(Long boardId) {
        return queryFactory.select(
                Projections.constructor(
                    ReviewBadgeDto.class,
                    review.badgeTaste,
                    review.badgeBrix,
                    review.badgeTexture
                )
            )
            .from(review)
            .where(review.boardId.eq(boardId).and(notDeleted()))
            .fetch();
    }


    @Override
    public List<ReviewStatisticDao> getReviewStatisticByBoardIds(List<Long> boardReviewUpdateId) {
        return queryFactory
            .select(new QReviewStatisticDao(
                review.boardId,
                review.rate.avg(),
                review.count()))
            .from(review)
            .where(review.boardId.in(boardReviewUpdateId))
            .groupBy(review.boardId)
            .fetch();
    }

    @Override
    public List<AiLearningReviewDto> findAiLearningData(Integer offset, Integer limit) {
        return queryFactory.select(
                new QAiLearningReviewDto(
                    review.boardId,
                    review.content
                )
            )
            .from(review)
            .offset(offset)
            .limit(limit)
            .orderBy(review.boardId.asc())
            .fetch();
    }
}
