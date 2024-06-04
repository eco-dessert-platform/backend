package com.bbangle.bbangle.review.service;

import com.bbangle.bbangle.common.domain.Badge;
import com.bbangle.bbangle.common.image.service.S3Service;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.review.domain.Review;
import com.bbangle.bbangle.review.domain.ReviewImg;
import com.bbangle.bbangle.review.dto.ReviewDto;
import com.bbangle.bbangle.review.dto.ReviewRequest;
import com.bbangle.bbangle.review.dto.SummarizedReviewResponse;
import com.bbangle.bbangle.review.repository.ReviewImgRepository;
import com.bbangle.bbangle.review.repository.ReviewRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImgRepository reviewImgRepository;
    private final MemberRepository memberRepository;
    private final S3Service s3Service;
    private final ReviewStatistics reviewStatistics;
    private static final String BOARD_FOLDER = "board/";
    private static final String REVIEW_FOLDER = "/review/";

    @Transactional
    public void makeReview(ReviewRequest reviewRequest, Long memberId) {
        memberRepository.findMemberById(memberId);
        Review review = Review.builder()
            .content(reviewRequest.content())
            .rate(reviewRequest.rate())
            .memberId(memberId)
            .boardId(reviewRequest.boardId())
            .build();
        List<Badge> badges = reviewRequest.badges();
        badges.forEach(review::insertBadge);
        reviewRepository.save(review);
        if (Objects.isNull(reviewRequest.photos())) {
            return;
        }
        makeReviewImg(reviewRequest, review);
    }

    private void makeReviewImg(ReviewRequest reviewRequest, Review review) {
        Long reviewId = review.getId();
        Long boardId = reviewRequest.boardId();
        List<MultipartFile> photos = reviewRequest.photos();
        photos.stream()
            .map(photo -> s3Service.saveImage(photo,
                BOARD_FOLDER + boardId + REVIEW_FOLDER + reviewId))
            .forEach(reviewImgPath -> reviewImgRepository.save(ReviewImg.builder()
                .reviewId(reviewId)
                .url(reviewImgPath)
                .build()));
    }

    public SummarizedReviewResponse getSummarizedReview(Long boardId) {
        List<ReviewDto> reviews = reviewRepository.findByBoardId(boardId);

        if (reviews.isEmpty()) {
            return SummarizedReviewResponse.getEmpty();
        }

        BigDecimal averageRatingScore = reviewStatistics.getAverageRatingScore(reviews);
        int reviewCount = reviewStatistics.count(reviews);
        List<String> popularBadgeList = reviewStatistics.getPopularBadgeList(reviews);

        return SummarizedReviewResponse.of(averageRatingScore, reviewCount, popularBadgeList);
    }
}
