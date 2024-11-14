package com.bbangle.bbangle.review.service;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.boardstatistic.service.BoardStatisticService;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.ReviewFixture;
import com.bbangle.bbangle.fixture.ReviewRequestFixture;
import com.bbangle.bbangle.image.domain.Image;
import com.bbangle.bbangle.image.domain.ImageCategory;
import com.bbangle.bbangle.image.dto.ImageDto;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.page.ImageCustomPage;
import com.bbangle.bbangle.page.ReviewCustomPage;
import com.bbangle.bbangle.review.domain.Badge;
import com.bbangle.bbangle.review.domain.Review;
import com.bbangle.bbangle.review.domain.QReview;
import com.bbangle.bbangle.review.domain.ReviewLike;
import com.bbangle.bbangle.review.dto.BrixDto;
import com.bbangle.bbangle.review.dto.ReviewImageUploadRequest;
import com.bbangle.bbangle.review.dto.ReviewImageUploadResponse;
import com.bbangle.bbangle.review.dto.ReviewImagesResponse;
import com.bbangle.bbangle.review.dto.ReviewInfoResponse;
import com.bbangle.bbangle.review.dto.ReviewRateResponse;
import com.bbangle.bbangle.review.dto.ReviewRequest;
import com.bbangle.bbangle.review.dto.SummarizedReviewResponse;
import com.bbangle.bbangle.review.dto.TasteDto;
import com.bbangle.bbangle.review.dto.TextureDto;
import com.bbangle.bbangle.review.repository.ReviewLikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.ion.Decimal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.bbangle.bbangle.review.domain.Badge.*;
import static org.assertj.core.api.Assertions.assertThat;

class ReviewServiceTest extends AbstractIntegrationTest {

    private static final BigDecimal DEFAULT_REVIEW_RATE = new BigDecimal("4.0");
    @Value("${cdn.domain}")
    private String cdnDomain;

    @Autowired
    ReviewService reviewService;

    @Autowired
    ReviewLikeRepository reviewLikeRepository;

    @Autowired
    private BoardStatisticService boardStatisticService;

    Board board;
    Member member;

    @BeforeEach
    void setUp() {
        reviewLikeRepository.deleteAllInBatch();
        Member testUser = Member.builder()
            .name("testUser")
            .email("test@test.com")
            .isDeleted(false)
            .build();
        member = memberRepository.save(testUser);

        board = Board.builder()
            .isDeleted(false)
            .title("board1")
            .build();
        board = boardRepository.save(board);
        BoardStatistic boardStatistic = BoardStatisticFixture.newBoardStatistic(board);
        boardStatisticRepository.save(boardStatistic);
    }

    @Test
    @DisplayName("리뷰 insert 에 성공한다")
    void testReviewSuccess() {
        //given
        List<Badge> badges = new ArrayList<>();
        badges.add(GOOD);
        badges.add(PLAIN);
        badges.add(SOFT);
        ReviewRequest reviewRequest = makeReviewRequest(badges);
        List<Member> members = memberRepository.findAll();

        //when
        reviewService.makeReview(reviewRequest, members.get(0)
            .getId());
        List<Review> reviewList = reviewRepository.findAll();

        //then
        assertThat(reviewList).hasSize(1);
        assertThat(reviewList.get(0)
            .getBadgeTaste()).isEqualTo(GOOD);
        assertThat(reviewList.get(0)
            .getBadgeBrix()).isEqualTo(PLAIN);
        assertThat(reviewList.get(0)
            .getBadgeTexture()).isEqualTo(SOFT);
    }

    @Test
    @DisplayName("리뷰 이미지 업로드에 성공한다")
    void uploadImages() {
        //given
        ReviewImageUploadRequest reviewImageUploadRequest =
            new ReviewImageUploadRequest(List.of(createMockMultipartFile()),
                ImageCategory.REVIEW);
        Long memberId = member.getId();
        //when
        ReviewImageUploadResponse reviewImageUploadResponse = reviewService.uploadReviewImage(
            reviewImageUploadRequest, memberId);

        //then
        assertThat(reviewImageUploadResponse.urls()).hasSize(1);
    }

    @Test
    @DisplayName("리뷰 삭제에 성공한다.")
    void deleteReview() {
        //given
        ReviewRequest reviewRequest = ReviewRequestFixture.createReviewRequest(board.getId());
        reviewService.makeReview(reviewRequest, member.getId());
        Review targetReview = getTargetReview();
        Long targetReviewId = targetReview.getId();
        createReviewLike(targetReviewId);
        createReviewImage(targetReviewId);

        //when
        reviewService.deleteReview(targetReviewId, member.getId());
        BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(board.getId())
            .orElseThrow();

        //then
        assertThat(boardStatistic.getBoardReviewCount()).isZero();
        assertThat(boardStatistic.getBoardReviewGrade()).isZero();
    }

    private void createReviewImage(Long targetReviewId) {
        Image image = Image.builder()
            .imageCategory(ImageCategory.REVIEW)
            .path("test")
            .order(0)
            .domainId(targetReviewId)
            .build();
        imageRepository.save(image);
    }

    private void createReviewLike(Long targetReviewId) {
        ReviewLike reviewLike = ReviewLike.builder()
            .reviewId(targetReviewId)
            .memberId(member.getId())
            .build();
        reviewLikeRepository.save(reviewLike);
    }

    private Review getTargetReview() {
        return queryFactory.select(QReview.review)
            .from(QReview.review)
            .where(QReview.review.boardId.eq(board.getId())
                .and(QReview.review.memberId.eq(member.getId())))
            .fetchOne();
    }

    private ReviewRequest makeReviewRequest(List<Badge> badges) {
        List<Board> boards = boardRepository.findAll();
        List<String> photos = new ArrayList<>();
        photos.add("test");
        return new ReviewRequest(badges, DEFAULT_REVIEW_RATE, null,
            boards.get(0)
                .getId(), photos);
    }

    @DisplayName("평점이 포함된 리뷰 조회에 성공한다")
    @Test
    void getReviewRate() {
        //given
        Long boardId = board.getId();
        Review review = ReviewFixture.createReviewWithBoardIdAndRate(boardId, 4);
        reviewRepository.save(review);

        //when
        ReviewRateResponse reviewRate = reviewService.getReviewRate(boardId);

        //then
        assertThat(reviewRate)
            .extracting("taste", "brix", "texture")
            .containsExactly(
                new TasteDto(1, 0),
                new BrixDto(1, 0),
                new TextureDto(1, 0));
    }

    @DisplayName("상세 리뷰 목록 조회에 성공한다")
    @Test
    void getReviews() {
        //given
        Long boardId = board.getId();
        Long memberId = member.getId();
        createReviewList(boardId, 5);

        //when
        ReviewCustomPage<List<ReviewInfoResponse>> reviews = reviewService.getReviews(boardId, null,
            memberId);

        //then
        assertThat(reviews.getContent().get(0))
            .extracting("tags", "like", "isLiked")
            .containsExactly(
                List.of("맛있어요", "달아요", "부드러워요"), 1, false
            );
        assertThat(reviews.getContent()).hasSize(5);
        assertThat(reviews.getHasNext()).isFalse();
    }

    private void createReviewList(Long boardId, int reviewCount) {
        for (int i = 0; i < reviewCount; i++) {
            Review review = ReviewFixture.createReviewWithBoardIdAndRate(boardId, 4.0);
            Review savedReview = reviewRepository.save(review);
            ReviewLike reviewLike = ReviewFixture.createReviewLike(savedReview, (long) reviewCount);
            reviewLikeRepository.save(reviewLike);
        }
    }

    private MockMultipartFile createMockMultipartFile() {
        return new MockMultipartFile(
            "리뷰 이미지",
            "testImage.png",
            MediaType.IMAGE_PNG_VALUE,
            "testImage".getBytes());
    }

    @DisplayName("리뷰 좋아요 등록에 성공한다")
    @Test
    void insertLike() {
        // given
        Long memberId = member.getId();
        Long boardId = board.getId();
        Review review = ReviewFixture.createReviewWithBoardIdAndRate(boardId, 4.0);
        Review savedReview = reviewRepository.save(review);

        // when
        reviewService.insertLike(savedReview.getId(), memberId);
        List<ReviewLike> reviewLikes = reviewLikeRepository.findAll();

        // then
        assertThat(reviewLikes).hasSize(1);
    }

    @DisplayName("리뷰 좋아요 해제에 성공한다")
    @Test
    void removeLike() {
        // given
        Long memberId = member.getId();
        Long boardId = board.getId();
        Review review = ReviewFixture.createReviewWithBoardIdAndRate(boardId, 4.0);
        Review savedReview = reviewRepository.save(review);

        // when
        reviewService.insertLike(savedReview.getId(), memberId);
        reviewService.removeLike(savedReview.getId(), memberId);
        List<ReviewLike> reviewLikes = reviewLikeRepository.findAll();

        // then
        assertThat(reviewLikes).isEmpty();
    }

    @DisplayName("리뷰 상세 정보 조회에 성공한다")
    @Test
    void getReviewDetail() {
        // given
        Long memberId = member.getId();
        Long boardId = board.getId();
        createReviewList(boardId, 1);
        Long reviewId = reviewRepository.findAll().stream()
            .findFirst()
            .get()
            .getId();
        // when
        ReviewInfoResponse reviewDetail = reviewService.getReviewDetail(reviewId, memberId);

        // then
        assertThat(reviewDetail).isNotNull();
    }

    @DisplayName("리뷰에 있는 이미지 조회에 성공한다")
    @Test
    void getReviewImages() {
        // given
        createImageEntityList(3, 1L);

        // when
        ReviewImagesResponse reviewImages = reviewService.getReviewImages(1L);

        // then
        assertThat(reviewImages.previewImages().get(0)).isEqualTo(cdnDomain + "testPath");
        assertThat(reviewImages.previewImages()).hasSize(3);
    }

    @DisplayName("내가 작성한 리뷰 조회에 성공한다")
    @Test
    void getMyReviews() {
        // given
        Long boardId = board.getId();
        createReviewList(boardId, 11);

        // when
        ReviewCustomPage<List<ReviewInfoResponse>> myReviews = reviewService.getMyReviews(1L, null);

        // then
        assertThat(myReviews.getHasNext()).isTrue();
        assertThat(myReviews.getContent()).hasSize(10);
    }

    @DisplayName("상품 게시물에 있는 모든 리뷰 이미지 조회에 성공한다")
    @Test
    void getAllImagesByBoardId() {
        // given
        Long boardId = board.getId();
        Review review = ReviewFixture.createReviewWithBoardIdAndRate(boardId, 4.0);
        Review savedReview = reviewRepository.save(review);
        createImageEntityList(4, savedReview.getId());

        // when
        ImageCustomPage<List<ImageDto>> allImages =
            reviewService.getAllImagesByBoardId(boardId, null);

        // then
        assertThat(allImages.getContent()).hasSize(4);
        assertThat(allImages.getHasNext()).isFalse();
        assertThat(allImages.getContent().get(0))
            .extracting("url")
            .isEqualTo(cdnDomain + "testPath");

    }

    @DisplayName("리뷰 업데이트에 성공한다")
    @Test
    void updateReview() {
        // given
        Long boardId = board.getId();
        Long memberId = member.getId();
        Review review = ReviewFixture.createReviewWithBoardIdAndRateAndMember(boardId, 4.0,
            memberId);
        Review savedReview = reviewRepository.save(review);
        List<Badge> badges = new ArrayList<>();
        badges.add(BAD);
        badges.add(SWEET);
        badges.add(DRY);
        ReviewRequest reviewRequest = makeReviewRequest(badges);

        // when
        reviewService.updateReview(reviewRequest, savedReview.getId(), memberId);
        Review updatedReview = reviewRepository.findById(savedReview.getId()).get();

        // then
        assertThat(updatedReview)
            .extracting("badgeTaste", "badgeBrix", "badgeTexture")
            .containsExactly(BAD, SWEET, DRY);
    }

    @DisplayName("리뷰 이미지 삭제에 성공한다")
    @Test
    void deleteReviewImage() {
        // given
        Long boardId = board.getId();
        Review review = ReviewFixture.createReviewWithBoardIdAndRate(boardId, 4.0);
        Review savedReview = reviewRepository.save(review);
        createImageEntityList(1, savedReview.getId());

        // when
        List<Image> images = imageRepository.findAll();
        images.forEach(image -> reviewService.deleteImage(image.getId()));
        List<Image> testImages = imageRepository.findAll();

        // then
        assertThat(testImages).isEmpty();
    }

    private void createImageEntityList(int size, Long domainId) {
        for (int i = 0; i < size; i++) {
            Image image = Image.builder()
                .domainId(domainId)
                .imageCategory(ImageCategory.REVIEW)
                .order(i)
                .path("testPath")
                .build();
            imageRepository.save(image);
        }
    }

    @Nested
    @DisplayName("getSummarizedReview 메서드는")
    class GetSummarizedReview {

        Board targetBoard;

        @BeforeEach
        void init() {
            targetBoard = fixtureBoard(Map.of());
        }

        @Test
        @DisplayName("긍정 뱃지 리뷰가 많으면 긍정 뱃지 리스트를 반환한다")
        void getPositiveBadgeTest() {
            fixtureReview(Map.of(
                "boardId", targetBoard.getId(),
                "badgeTaste", Badge.GOOD,
                "badgeBrix", SWEET,
                "badgeTexture", SOFT,
                "rate", BigDecimal.valueOf(4.5)
            ));

            fixtureReview(Map.of(
                "boardId", targetBoard.getId(),
                "badgeTaste", GOOD,
                "badgeBrix", SWEET,
                "badgeTexture", SOFT,
                "rate", BigDecimal.valueOf(4.5)
            ));

            SummarizedReviewResponse response = reviewService.getSummarizedReview(
                targetBoard.getId());

            boardStatisticService.updatingNonRankedBoards();

            assertThat(response.getBadges()).isEqualTo(
                List.of("GOOD", "SWEET", "SOFT"));
        }

        @Test
        @DisplayName("부정 뱃지 리뷰가 많으면 부정 뱃지 리스트를 반환한다")
        void getNagativeBadgeTest() {
            fixtureReview(Map.of(
                "boardId", targetBoard.getId(),
                "badgeTaste", BAD,
                "badgeBrix", PLAIN,
                "badgeTexture", DRY,
                "rate", BigDecimal.valueOf(4.5)
            ));

            fixtureReview(Map.of(
                "boardId", targetBoard.getId(),
                "badgeTaste", BAD,
                "badgeBrix", PLAIN,
                "badgeTexture", DRY,
                "rate", BigDecimal.valueOf(4.5)
            ));

            SummarizedReviewResponse response = reviewService.getSummarizedReview(
                targetBoard.getId());

            assertThat(response.getBadges()).isEqualTo(
                List.of("BAD", "PLAIN", "DRY"));
        }

        @Test
        @DisplayName("부정 뱃지 리뷰가 많으면 부정 뱃지 리스트를 반환한다")
        void getEquleBadgeTest() {
            fixtureReview(Map.of(
                "boardId", targetBoard.getId(),
                "badgeTaste", GOOD,
                "badgeBrix", SWEET,
                "badgeTexture", SOFT,
                "rate", BigDecimal.valueOf(4.5)
            ));

            fixtureReview(Map.of(
                "boardId", targetBoard.getId(),
                "badgeTaste", BAD,
                "badgeBrix", PLAIN,
                "badgeTexture", DRY,
                "rate", BigDecimal.valueOf(4.5)
            ));

            boardStatisticService.updatingNonRankedBoards();

            SummarizedReviewResponse response = reviewService.getSummarizedReview(
                targetBoard.getId());

            assertThat(response.getBadges()).isEqualTo(
                List.of("GOOD", "SWEET", "SOFT"));
        }

        @Test
        @DisplayName("총 리뷰의 평균 값을 소수점 2자리까지 출력한다")
        void getAvarageRatingScore() {
            float[] score = new float[]{5f, 3.5f};

            fixtureReview(Map.of(
                "boardId", targetBoard.getId(),
                "rate", BigDecimal.valueOf(score[0])
            ));

            fixtureReview(Map.of(
                "boardId", targetBoard.getId(),
                "rate", BigDecimal.valueOf(score[1])
            ));

            boardStatisticService.updatingNonRankedBoards();

            SummarizedReviewResponse response = reviewService.getSummarizedReview(
                targetBoard.getId());

            assertThat(response.getCount()).isEqualTo(2);

            assertThat(response.getRating()).isEqualTo(
                Decimal.valueOf((score[0] + score[1]))
                    .divide(
                        BigDecimal.valueOf(score.length), 2, RoundingMode.DOWN));
        }
    }
}
