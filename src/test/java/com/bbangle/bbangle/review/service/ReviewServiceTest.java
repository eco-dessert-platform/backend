package com.bbangle.bbangle.review.service;

import static com.bbangle.bbangle.review.domain.Badge.BAD;
import static com.bbangle.bbangle.review.domain.Badge.DRY;
import static com.bbangle.bbangle.review.domain.Badge.GOOD;
import static com.bbangle.bbangle.review.domain.Badge.SOFT;
import static com.bbangle.bbangle.review.domain.Badge.SWEET;
import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.common.page.ImageCustomPage;
import com.bbangle.bbangle.common.page.ReviewCustomPage;
import com.bbangle.bbangle.fixture.ReviewFixture;
import com.bbangle.bbangle.fixturemonkey.FixtureMonkeyConfig;
import com.bbangle.bbangle.image.customer.dto.ImageDto;
import com.bbangle.bbangle.image.domain.Image;
import com.bbangle.bbangle.image.domain.ImageCategory;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.review.customer.dto.BrixDto;
import com.bbangle.bbangle.review.customer.dto.ReviewImageUploadRequest;
import com.bbangle.bbangle.review.customer.dto.ReviewImageUploadResponse;
import com.bbangle.bbangle.review.customer.dto.ReviewImagesResponse;
import com.bbangle.bbangle.review.customer.dto.ReviewInfoResponse;
import com.bbangle.bbangle.review.customer.dto.ReviewRateResponse;
import com.bbangle.bbangle.review.customer.dto.ReviewRequest;
import com.bbangle.bbangle.review.customer.dto.SummarizedReviewResponse;
import com.bbangle.bbangle.review.customer.dto.TasteDto;
import com.bbangle.bbangle.review.customer.dto.TextureDto;
import com.bbangle.bbangle.review.customer.service.ReviewService;
import com.bbangle.bbangle.review.domain.Badge;
import com.bbangle.bbangle.review.domain.QReview;
import com.bbangle.bbangle.review.domain.Review;
import com.bbangle.bbangle.review.domain.ReviewLike;
import com.bbangle.bbangle.review.repository.ReviewLikeRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

class ReviewServiceTest extends AbstractIntegrationTest {

    private static final BigDecimal DEFAULT_REVIEW_RATE = new BigDecimal("4.0");
    @Value("${cdn.domain}")
    private String cdnDomain;
    @Autowired
    ReviewService reviewService;

    @Autowired
    ReviewLikeRepository reviewLikeRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BoardRepository boardRepository;

    @BeforeEach
    void setUp() {
        reviewLikeRepository.deleteAllInBatch();
    }

    // 테스트에서 공통으로 사용할 메서드들
    private Member createMember() {
        Member testUser = FixtureMonkeyConfig.fixtureMonkey.giveMeOne(Member.class);
        return memberRepository.save(testUser);
    }

    private Board createBoard() {
        Board board = FixtureMonkeyConfig.fixtureMonkey.giveMeOne(Board.class);
        Board savedBoard = boardRepository.save(board);
        return savedBoard;
    }

    @Test
    @DisplayName("리뷰 insert 에 성공한다")
    void testReviewSuccess() {
        //given
        Member member = createMember();
        ReviewRequest reviewRequest = FixtureMonkeyConfig.fixtureMonkey.giveMeOne(
            ReviewRequest.class);

        //when
        reviewService.makeReview(reviewRequest, member.getId());
        List<Review> reviewList = reviewRepository.findAll();

        //then
        assertThat(reviewList).hasSize(1);
    }

    @Test
    @DisplayName("리뷰 이미지 업로드에 성공한다")
    void uploadImages() {
        //given
        Member member = createMember();
        ReviewImageUploadRequest request = FixtureMonkeyConfig.fixtureMonkey.giveMeOne(
            ReviewImageUploadRequest.class);

        //when
        ReviewImageUploadResponse reviewImageUploadResponse = reviewService.uploadReviewImage(
            request, member.getId());

        //then
        assertThat(reviewImageUploadResponse.urls()).isNotEmpty();
    }

    @Test
    @DisplayName("리뷰 소프트 삭제에 성공한다.")
    void deleteReview() {
//        //given
//        Member member = createMember();
//        Board board1 = fixtureBoard(emptyMap());
//        fixtureReview(Map.of("boardId", board1.getId()))
//        ReviewRequest reviewRequest = FixtureMonkeyConfig.fixtureMonkey.giveMeBuilder(ReviewRequest.class)
//                .set("boardId", board1.getId())
//                .sample();
//        reviewService.makeReview(reviewRequest, member.getId());
//        Review review = getTargetReview(member, board);
//
//        //when
//        reviewService.deleteReview(review.getId(), member.getId());
//
//        //then
//        assertThat(review.isDeleted()).isFalse();
    }

    private Review getTargetReview(Member member, Board board) {
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
        Board board = createBoard();
        Long boardId = board.getId();
        Review review = FixtureMonkeyConfig.fixtureMonkey.giveMeBuilder(Review.class)
            .set("badgeBrix", SWEET)
            .set("badgeTaste", GOOD)
            .set("badgeTexture", SOFT)
            .set("boardId", boardId)
            .set("isDeleted", false)
            .sample();
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
        Member member = createMember();
        Board board = createBoard();
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

    @DisplayName("리뷰 좋아요 등록에 성공한다")
    @Test
    void insertLike() {
        // given
        Member member = createMember();
        Board board = createBoard();
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

    @Test
    @DisplayName("동시에 한 리뷰에 대해 좋아요를 할 경우 한번만 요청에 성공해 데이터를 저장한다")
    void concurrentLike() {
        //given
        Member member = memberRepository.save(FixtureMonkeyConfig.fixtureMonkey
            .giveMeOne(Member.class));

        Review review = reviewRepository.save(FixtureMonkeyConfig.fixtureMonkey
            .giveMeBuilder(Review.class)
            .set("memberId", member.getId())
            .sample());

        final int threadCount = 2;
        final ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        //when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    reviewService.insertLike(review.getId(), member.getId());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        //then
        Assertions.assertThatCode(() ->
                reviewLikeRepository.findByMemberIdAndReviewId(member.getId(), review.getId()))
            .doesNotThrowAnyException();
    }

    @DisplayName("리뷰 좋아요 해제에 성공한다")
    @Test
    void removeLike() {
        // given
        Member member = createMember();
        Board board = createBoard();
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
        Member member = createMember();
        Board board = createBoard();
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
        Board board = createBoard();
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
        Board board = createBoard();
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
        Member member = createMember();
        Board board = createBoard();
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
        Board board = createBoard();
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

        @BeforeEach
        void init() {
            boardStatisticRepository.deleteAll();
        }

        @Test
        @DisplayName("긍정 뱃지 리뷰가 많으면 긍정 뱃지 리스트를 반환한다")
        void getPositiveBadgeTest() {
            Board board = createBoard();
            Long boardId = board.getId();
            List<Review> goodReviews = FixtureMonkeyConfig.fixtureMonkey.giveMeBuilder(Review.class)
                .set("boardId", boardId)
                .set("badgeTaste", Badge.GOOD)
                .set("isDeleted", false)
                .sampleList(3);
            System.out.println("goodbaget size: " + goodReviews.size());
            List<Review> badReviews = FixtureMonkeyConfig.fixtureMonkey.giveMeBuilder(Review.class)
                .set("boardId", boardId)
                .set("badgeTaste", Badge.BAD)
                .set("isDeleted", false)
                .sampleList(2);
            System.out.println("bad size: " + badReviews.size());

            reviewRepository.saveAll(goodReviews);
            reviewRepository.saveAll(badReviews);

            SummarizedReviewResponse response = reviewService.getSummarizedReview(boardId);

            assertThat(response.getBadges()).contains("GOOD");
        }

        @Test
        @DisplayName("부정 뱃지 리뷰가 많으면 부정 뱃지 리스트를 반환한다")
        void getNagativeBadgeTest() {
            Board board = createBoard();
            Long boardId = board.getId();
            List<Review> goodReviews = FixtureMonkeyConfig.fixtureMonkey.giveMeBuilder(Review.class)
                .set("boardId", boardId)
                .set("badgeTaste", Badge.GOOD)
                .set("isDeleted", false)
                .sampleList(2);
            List<Review> badReviews = FixtureMonkeyConfig.fixtureMonkey.giveMeBuilder(Review.class)
                .set("boardId", boardId)
                .set("badgeTaste", Badge.BAD)
                .set("isDeleted", false)
                .sampleList(3);
            reviewRepository.saveAll(goodReviews);
            reviewRepository.saveAll(badReviews);

            SummarizedReviewResponse response = reviewService.getSummarizedReview(boardId);

            assertThat(response.getBadges()).contains("BAD");
        }

        @Test
        @DisplayName("총 리뷰의 평균 값을 소수점 2자리까지 출력한다")
        void getAvarageRatingScore() {
            //given
            BoardStatistic boardStatistic1 = fixtureRanking(
                Map.of("boardReviewGrade", BigDecimal.valueOf(4.25f), "boardReviewCount", 2L));
            Board board = fixtureBoard(Map.of("boardStatistic", boardStatistic1));
            boardRepository.save(board);
            Long boardId = board.getId();

            Review review1 = FixtureMonkeyConfig.fixtureMonkey.giveMeBuilder(Review.class)
                .set("boardId", boardId)
                .set("rate", BigDecimal.valueOf(5f))
                .set("isDeleted", false)
                .sample();
            Review review2 = FixtureMonkeyConfig.fixtureMonkey.giveMeBuilder(Review.class)
                .set("boardId", boardId)
                .set("rate", BigDecimal.valueOf(3.5f))
                .set("isDeleted", false)
                .sample();
            reviewRepository.save(review1);
            reviewRepository.save(review2);

            //when
            SummarizedReviewResponse response = reviewService.getSummarizedReview(boardId);

            //then
            assertThat(response.getCount()).isEqualTo(2);
            assertThat(response.getRating()).isEqualTo(BigDecimal.valueOf(4.25));
        }
    }
}
