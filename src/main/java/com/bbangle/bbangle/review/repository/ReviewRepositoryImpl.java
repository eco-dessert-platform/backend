package com.bbangle.bbangle.review.repository;

import com.bbangle.bbangle.member.domain.QMember;
import com.bbangle.bbangle.review.domain.*;
import com.bbangle.bbangle.review.dto.QReviewSingleDto;
import com.bbangle.bbangle.review.dto.ReviewSingleDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewQueryDSLRepository{
    private final JPAQueryFactory queryFactory;
    private static final QReview review = QReview.review;
    private static final QMember member = QMember.member;
    private static final QReviewImg reviewImg = QReviewImg.reviewImg;
    private static final QReviewLike reviewLike = QReviewLike.reviewLike;
    private static final Long PAGE_SIZE = 10L;

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
                                review.createdAt
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
    public Map<Long, List<String>> getImageMap(ReviewCursor reviewCursor) {
        BooleanBuilder imageCondition = getImageCondition(reviewCursor);
        List<Tuple> reviewImages = queryFactory.select(
                        review.id,
                        reviewImg.url
                )
                .from(review)
                .leftJoin(reviewImg).on(reviewImg.reviewId.eq(review.id))
                .where(imageCondition)
                .fetch();
        return createImageMap(reviewImages);
    }

    private BooleanBuilder getImageCondition(ReviewCursor reviewCursor) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if(reviewCursor.reviewId() != null){
            booleanBuilder.and(review.id.eq(reviewCursor.reviewId()));
        }else {
            booleanBuilder.and(review.id.between(reviewCursor.nextCursor(), reviewCursor.endCursor()));
        }
        return booleanBuilder;
    }

    @Override
    public List<ReviewLike> getLikeList(ReviewCursor reviewCursor) {
        BooleanBuilder likeCondition = getLikeCondition(reviewCursor);
        return queryFactory.selectFrom(reviewLike)
                .where(likeCondition)
                .fetch();
    }

    private BooleanBuilder getLikeCondition(ReviewCursor reviewCursor) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if(reviewCursor.reviewId() != null){
            booleanBuilder.and(reviewLike.reviewId.eq(reviewCursor.reviewId()));
        }else {
            booleanBuilder.and(reviewLike.reviewId.between(reviewCursor.nextCursor(), reviewCursor.endCursor()));
        }
        return booleanBuilder;
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

    private Map<Long, List<String>> createImageMap(List<Tuple> reviewImages) {
        return reviewImages
                .stream()
                .collect(Collectors.toMap(
                        reviewImage -> reviewImage.get(review.id),
                        reviewImage -> {
                            List<String> images = new ArrayList<>();
                            images.add(reviewImage.get(reviewImg.url));
                            return images;
                        },
                        (existImages, newImage) -> {
                            existImages.addAll(newImage);
                            return existImages;
                        }
                ));
    }

    @Override
    public ReviewSingleDto getReviewDetail(Long reviewId, Long memberId) {
        return getPureReviewSingleDto()
                .where(eqId(reviewId).and(eqMemberId(memberId)).and(notDeleted()))
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
}
