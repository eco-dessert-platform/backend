package com.bbangle.bbangle.review.repository;


import com.bbangle.bbangle.analytics.dto.AnalyticsCumulationResponseDto;
import com.bbangle.bbangle.analytics.dto.DateAndCountDto;
import com.bbangle.bbangle.analytics.dto.QDateAndCountDto;
import com.bbangle.bbangle.analytics.dto.QAnalyticsCumulationResponseDto;
import com.bbangle.bbangle.review.domain.QReview;
import com.bbangle.bbangle.review.domain.QReviewImg;
import com.bbangle.bbangle.review.domain.ReviewCursor;
import com.bbangle.bbangle.review.domain.ReviewLike;
import com.bbangle.bbangle.review.domain.QReviewLike;
import com.bbangle.bbangle.review.dto.QReviewImgDto;
import com.bbangle.bbangle.review.dto.QReviewSingleDto;
import com.bbangle.bbangle.review.dto.ReviewImgDto;
import com.bbangle.bbangle.review.dto.ReviewSingleDto;
import com.bbangle.bbangle.review.dto.ReviewCountPerBoardIdDto;
import com.bbangle.bbangle.review.dto.QReviewCountPerBoardIdDto;
import com.bbangle.bbangle.review.dto.LikeCountPerReviewIdDto;
import com.bbangle.bbangle.review.dto.QLikeCountPerReviewIdDto;
import com.bbangle.bbangle.review.dto.ReviewDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.bbangle.bbangle.member.domain.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewQueryDSLRepository{
    private static final QReview review = QReview.review;
    private static final QMember member = QMember.member;
    private static final QReviewImg reviewImg = QReviewImg.reviewImg;
    private static final QReviewLike reviewLike = QReviewLike.reviewLike;
    private static final Long PAGE_SIZE = 10L;

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;


    private JPAQuery<ReviewSingleDto> getPureReviewSingleDto() {
        return queryFactory
                .select(
                        new QReviewSingleDto(
                                review.id,
                                member.nickname,
                                review.rate,
                                review.badgeTaste,
                                review.badgeBrix,
                                review.badgeTexture,
                                review.content,
                                review.createdAt,
                                review.isBest
                        )
                )
                .from(review)
                .leftJoin(member).on(review.memberId.eq(member.id));
    }

    @Override
    public List<ReviewSingleDto> getReviewSingleList(Long boardId, Long cursorId) {
        return getPureReviewSingleDto()
                .where(eqBoardId(boardId).and(getCursorCondition(cursorId)))
                .orderBy(review.createdAt.desc())
                .limit(PAGE_SIZE +1)
                .fetch();
    }

    @Override
    public Map<Long, List<ReviewImgDto>> getImageMap(ReviewCursor reviewCursor) {
        BooleanBuilder imageCondition = getImageCondition(reviewCursor);
        List<Tuple> reviewImages = queryFactory.select(
                        review.id,
                        reviewImg.id,
                        reviewImg.url
                )
                .from(review)
                .leftJoin(reviewImg).on(reviewImg.reviewId.eq(review.id))
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
                .limit(PAGE_SIZE +1)
                .fetch();
    }

    @Override
    public List<ReviewImgDto> getAllImagesByBoardId(Long boardId, Long requestCursor) {
        List<Long> fetch = queryFactory
                .select(review.id)
                .from(review)
                .where(eqBoardId(boardId))
                .fetch();
        BooleanBuilder reviewImgCondition = new BooleanBuilder();
        if(requestCursor != null){
            reviewImgCondition.and(reviewImg.id.loe(requestCursor));
        }
        reviewImgCondition.and(reviewImg.reviewId.in(fetch));
        return queryFactory
                .select(new QReviewImgDto(
                        reviewImg.id,
                        reviewImg.url
                ))
                .from(reviewImg)
                .where(reviewImgCondition)
                .orderBy(reviewImg.createdAt.desc())
                .limit(PAGE_SIZE + 1)
                .fetch();
    }

    @Override
    public List<ReviewCountPerBoardIdDto> getReviewCount(){
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
                .having(reviewLike.memberId.count().goe(minimumBestReviewStandard))
                .orderBy(reviewLike.memberId.count().desc())
                .fetch();
    }

    @Override
    public Map<Long, List<Long>> getBestReview(List<Long> reviewIds){
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
                .where(review.id.in(4))
                .execute();

        em.flush();
        em.clear();
    }

    @Override
    public List<DateAndCountDto> countReviewCreatedBetweenPeriod(LocalDate startLocalDate, LocalDate endLocalDate) {
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
    public List<AnalyticsCumulationResponseDto> countCumulatedReviewBeforeEndDate(LocalDate startLocalDate, LocalDate endLocalDate) {
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

    private BooleanBuilder getImageCondition(ReviewCursor reviewCursor) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if(reviewCursor.reviewId() != null){
            return booleanBuilder.and(review.id.eq(reviewCursor.reviewId()));
        }
        booleanBuilder.and(review.id.between(reviewCursor.nextCursor(), reviewCursor.lastCursor()));

        return booleanBuilder;
    }

    private Map<Long, List<ReviewImgDto>> createImageMap(List<Tuple> reviewImages) {
        return reviewImages
                .stream()
                .collect(toMap(
                        reviewImage -> reviewImage.get(review.id),
                        reviewImage -> {
                            List<ReviewImgDto> images = new ArrayList<>();
                            images.add(ReviewImgDto.builder()
                                    .id(reviewImage.get(reviewImg.id))
                                    .url(reviewImage.get(reviewImg.url))
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
        if(reviewCursor.reviewId() != null){
            return booleanBuilder.and(reviewLike.reviewId.eq(reviewCursor.reviewId()));
        }
        return booleanBuilder.and(reviewLike.reviewId.between(reviewCursor.nextCursor(), reviewCursor.lastCursor()));
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
