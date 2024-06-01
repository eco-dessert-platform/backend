package com.bbangle.bbangle.review.service;

import static com.bbangle.bbangle.image.domain.ImageCategory.REVIEW;

import com.bbangle.bbangle.common.domain.Badge;
import com.bbangle.bbangle.image.service.ImageService;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.review.domain.Review;
import com.bbangle.bbangle.review.dto.ReviewRequest;
import com.bbangle.bbangle.review.repository.ReviewRepository;
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
    private final MemberRepository memberRepository;
    private final ImageService imageService;

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

        saveReviewImage(reviewRequest, review);
    }

    private void saveReviewImage(ReviewRequest reviewRequest, Review review) {
        Long reviewId = review.getId();
        List<MultipartFile> photos = reviewRequest.photos();

        imageService.saveAll(REVIEW, photos, reviewId);
    }
}
