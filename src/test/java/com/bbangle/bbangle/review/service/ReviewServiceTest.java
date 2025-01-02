package com.bbangle.bbangle.review.service;

import static com.bbangle.bbangle.review.domain.Badge.BAD;
import static com.bbangle.bbangle.review.domain.Badge.DRY;
import static com.bbangle.bbangle.review.domain.Badge.GOOD;
import static com.bbangle.bbangle.review.domain.Badge.PLAIN;
import static com.bbangle.bbangle.review.domain.Badge.SOFT;
import static com.bbangle.bbangle.review.domain.Badge.SWEET;
import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.fixture.ReviewFixture;
import com.bbangle.bbangle.fixture.ReviewRequestFixture;
import com.bbangle.bbangle.fixturemonkey.FixtureMonkeyConfig;
import com.bbangle.bbangle.image.domain.Image;
import com.bbangle.bbangle.image.domain.ImageCategory;
import com.bbangle.bbangle.image.dto.ImageDto;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.page.ImageCustomPage;
import com.bbangle.bbangle.page.ReviewCustomPage;
import com.bbangle.bbangle.review.domain.Badge;
import com.bbangle.bbangle.review.domain.QReview;
import com.bbangle.bbangle.review.domain.Review;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    Board board;
    Member member;
    Long memberId;

    @BeforeEach
    void setUp() {
        reviewLikeRepository.deleteAllInBatch();
        Member testUser = FixtureMonkeyConfig.fixtureMonkey.giveMeOne(Member.class);
        member = memberRepository.save(testUser);
        memberId = member.getId();

        board = FixtureMonkeyConfig.fixtureMonkey.giveMeOne(Board.class);
        board = boardRepository.save(board);
        BoardStatistic boardStatistic1 = FixtureMonkeyConfig.fixtureMonkey.giveMeBuilder(BoardStatistic.class)
                .set("boardId", board.getId())
                .sample();
        boardStatisticRepository.save(boardStatistic1);
    }


    @Test
    @DisplayName("리뷰 insert 에 성공한다")
    void testReviewSuccess() {
        //given
        ReviewRequest reviewRequest = FixtureMonkeyConfig.fixtureMonkey.giveMeBuilder(ReviewRequest.class)
                .size("badges", 3)  // fixtureMonkey는 validation 어노테이션을 통해 정확한 값을 못 내려주기 때문에 여기서 설정
                .sample();
        Member member = FixtureMonkeyConfig.fixtureMonkey.giveMeOne(Member.class);
        memberRepository.save(member);

        //when
        reviewService.makeReview(reviewRequest, member.getId());
        List<Review> reviewList = reviewRepository.findAll();

        //then
        assertThat(reviewList).hasSize(1);
    }

    @Test
    @DisplayName("리뷰 이미지 업로드에 성공한다")
    void uploadImages(){
        //given
        ReviewImageUploadRequest request = FixtureMonkeyConfig.fixtureMonkey.giveMeOne(
                ReviewImageUploadRequest.class);
        Member member1 = FixtureMonkeyConfig.fixtureMonkey.giveMeOne(Member.class);
        memberRepository.save(member1);
        //when
        ReviewImageUploadResponse reviewImageUploadResponse = reviewService.uploadReviewImage(request, memberId);

        //then
        assertThat(reviewImageUploadResponse.urls()).isNotEmpty();
    }

    @Test
    @DisplayName("리뷰 소프트 삭제에 성공한다.")
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

        //then
        assertThat(targetReview.isDeleted()).isFalse();
    }

    private void createReviewImage(Long targetReviewId){
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
    void getReviewRate(){
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
                        new TextureDto(1,0));
    }

    @DisplayName("상세 리뷰 목록 조회에 성공한다")
    @Test
    void getReviews(){
        //given
        Long boardId = board.getId();
        Long memberId = member.getId();
        createReviewList(boardId, 5);

        //when
        ReviewCustomPage<List<ReviewInfoResponse>> reviews = reviewService.getReviews(boardId, null, memberId);

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
        for(int i = 0; i < reviewCount; i++) {
            Review review = ReviewFixture.createReviewWithBoardIdAndRate(boardId, 4.0);
            Review savedReview = reviewRepository.save(review);
            ReviewLike reviewLike = ReviewFixture.createReviewLike(savedReview, (long) reviewCount);
            reviewLikeRepository.save(reviewLike);
        }
    }

    @DisplayName("리뷰 좋아요 등록에 성공한다")
    @Test
    void insertLike(){
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
    void removeLike(){
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
    void getReviewDetail(){
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
    void getReviewImages(){
        // given
        createImageEntityList(3, 1L);

        // when
        ReviewImagesResponse reviewImages = reviewService.getReviewImages(1L);

        // then
        assertThat(reviewImages.previewImages().get(0)).isEqualTo(cdnDomain+"testPath");
        assertThat(reviewImages.previewImages()).hasSize(3);
    }

    @DisplayName("내가 작성한 리뷰 조회에 성공한다")
    @Test
    void getMyReviews(){
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
    void getAllImagesByBoardId(){
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
                .isEqualTo(cdnDomain+"testPath");

    }

    @DisplayName("리뷰 업데이트에 성공한다")
    @Test
    void updateReview(){
        // given
        Long boardId = board.getId();
        Long memberId = member.getId();
        Review review = ReviewFixture.createReviewWithBoardIdAndRateAndMember(boardId, 4.0, memberId);
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
    void deleteReviewImage(){
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
        for(int i = 0; i < size; i++){
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

            SummarizedReviewResponse response = reviewService.getSummarizedReview(
                targetBoard.getId());

            assertThat(response.getBadges()).isEqualTo(
                List.of("GOOD", "SWEET", "SOFT"));
        }

        @Test
        @DisplayName("총 리뷰의 평균 값을 소수점 2자리까지 출력한다")
        void getAvarageRatingScore() {
            //given
            Review review1 = FixtureMonkeyConfig.fixtureMonkey.giveMeBuilder(Review.class)
                    .set("boardId", targetBoard.getId())
                    .set("rate", BigDecimal.valueOf(5f))
                    .set("isDeleted", false)
                    .sample();
            Review review2 = FixtureMonkeyConfig.fixtureMonkey.giveMeBuilder(Review.class)
                    .set("boardId", targetBoard.getId())
                    .set("rate", BigDecimal.valueOf(3.5f))
                    .set("isDeleted", false)
                    .sample();
            reviewRepository.save(review1);
            reviewRepository.save(review2);

            BoardStatistic boardStatistic = FixtureMonkeyConfig.fixtureMonkey.giveMeBuilder(BoardStatistic.class)
                    .set("boardId", targetBoard.getId())
                    .set("boardReviewCount", 2L)
                    .set("boardReviewGrade", BigDecimal.valueOf(4.25f))
                    .sample();
            boardStatisticRepository.save(boardStatistic);

            //when
            SummarizedReviewResponse response = reviewService.getSummarizedReview(
                targetBoard.getId());

            //then
            assertThat(response.getCount()).isEqualTo(2);
            assertThat(response.getRating()).isEqualTo(BigDecimal.valueOf(4.25));
        }
    }
}
