package com.bbangle.bbangle.review;

import static com.bbangle.bbangle.review.domain.Badge.BAD;
import static com.bbangle.bbangle.review.domain.Badge.GOOD;
import static com.bbangle.bbangle.review.domain.Badge.HARD;
import static com.bbangle.bbangle.review.domain.Badge.PLAIN;
import static com.bbangle.bbangle.review.domain.Badge.SOFT;
import static com.bbangle.bbangle.review.domain.Badge.SWEET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.review.domain.Badge;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.review.domain.Review;
import com.bbangle.bbangle.review.domain.ReviewImg;
import com.bbangle.bbangle.review.dto.ReviewRequest;
import com.bbangle.bbangle.review.dto.SummarizedReviewResponse;
import com.bbangle.bbangle.review.repository.ReviewImgRepository;
import com.bbangle.bbangle.review.repository.ReviewRepository;
import com.bbangle.bbangle.review.service.ReviewService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.ion.Decimal;

class ReviewServiceTest extends AbstractIntegrationTest {

    @Autowired
    ReviewService reviewService;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    ReviewImgRepository reviewImgRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BoardRepository boardRepository;


    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        boardRepository.deleteAll();
        reviewRepository.deleteAll();
        Member testUser = Member.builder()
            .name("testUser")
            .email("test@test.com")
            .isDeleted(false)
            .build();
        memberRepository.save(testUser);

        Board board = Board.builder()
            .isDeleted(false)
            .title("board1")
            .build();
        boardRepository.save(board);

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
        List<Member> member = memberRepository.findAll();

        //when
        reviewService.makeReview(reviewRequest, member.get(0).getId());
        List<Review> reviewList = reviewRepository.findAll();
        List<ReviewImg> reviewImg = reviewImgRepository.findAll();

        //then
        assertThat(reviewList).hasSize(1);
        assertThat(reviewList.get(0).getBadgeTaste()).isEqualTo("GOOD");
        assertThat(reviewList.get(0).getBadgeBrix()).isEqualTo("PLAIN");
        assertThat(reviewList.get(0).getBadgeTexture()).isEqualTo("SOFT");
        assertThat(reviewImg).hasSize(1);
    }

    @Test
    @DisplayName("같은 위치의 리뷰 평가 뱃지가 존재하면 리뷰 insert 에 실패한다")
    void testReviewFail1() {
        //given
        List<Badge> badges = new ArrayList<>();
        badges.add(GOOD);
        badges.add(BAD);
        badges.add(SOFT);
        ReviewRequest reviewRequest = makeReviewRequest(badges);
        List<Member> member = memberRepository.findAll();

        //when, then
        assertThrows(RuntimeException.class, () ->
            reviewService.makeReview(reviewRequest, member.get(0).getId()
            ));
    }

    @Test
    @DisplayName("리뷰 평가 뱃지의 수가 부족하면 리뷰 insert 에 실패한다")
    void testReviewFail2() {
        //given
        List<Badge> badges = new ArrayList<>();
        badges.add(GOOD);
        badges.add(SOFT);
        ReviewRequest reviewRequest = makeReviewRequest(badges);
        List<Member> member = memberRepository.findAll();

        //when, then
        assertThrows(RuntimeException.class, () ->
            reviewService.makeReview(reviewRequest, member.get(0).getId())
        );
    }

    private ReviewRequest makeReviewRequest(List<Badge> badges) {
        List<Board> board = boardRepository.findAll();
        List<MultipartFile> photos = new ArrayList<>();
        MockMultipartFile photo = new MockMultipartFile(
            "test",
            "test.png",
            MediaType.IMAGE_PNG_VALUE,
            "test".getBytes()
        );
        photos.add(photo);
        return new ReviewRequest(badges, new BigDecimal("4.0"), null,
            board.get(0).getId(), photos);
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
                "badgeTaste", GOOD.name(),
                "badgeBrix", SWEET.name(),
                "badgeTexture", SOFT.name(),
                "rate", BigDecimal.valueOf(4.5)
            ));

            fixtureReview(Map.of(
                "boardId", targetBoard.getId(),
                "badgeTaste", GOOD.name(),
                "badgeBrix", SWEET.name(),
                "badgeTexture", SOFT.name(),
                "rate", BigDecimal.valueOf(4.5)
            ));

            SummarizedReviewResponse response = reviewService.getSummarizedReview(
                targetBoard.getId());

            assertThat(response.getBadges()).isEqualTo(
                List.of(GOOD.name(), SWEET.name(), SOFT.name()));
        }

        @Test
        @DisplayName("부정 뱃지 리뷰가 많으면 부정 뱃지 리스트를 반환한다")
        void getNagativeBadgeTest() {
            fixtureReview(Map.of(
                "boardId", targetBoard.getId(),
                "badgeTaste", BAD.name(),
                "badgeBrix", PLAIN.name(),
                "badgeTexture", HARD.name(),
                "rate", BigDecimal.valueOf(4.5)
            ));

            fixtureReview(Map.of(
                "boardId", targetBoard.getId(),
                "badgeTaste", BAD.name(),
                "badgeBrix", PLAIN.name(),
                "badgeTexture", HARD.name(),
                "rate", BigDecimal.valueOf(4.5)
            ));

            SummarizedReviewResponse response = reviewService.getSummarizedReview(
                targetBoard.getId());

            assertThat(response.getBadges()).isEqualTo(
                List.of(BAD.name(), PLAIN.name(), HARD.name()));
        }

        @Test
        @DisplayName("부정 뱃지 리뷰가 많으면 부정 뱃지 리스트를 반환한다")
        void getEquleBadgeTest() {
            fixtureReview(Map.of(
                "boardId", targetBoard.getId(),
                "badgeTaste", GOOD.name(),
                "badgeBrix", SWEET.name(),
                "badgeTexture", SOFT.name(),
                "rate", BigDecimal.valueOf(4.5)
            ));

            fixtureReview(Map.of(
                "boardId", targetBoard.getId(),
                "badgeTaste", BAD.name(),
                "badgeBrix", PLAIN.name(),
                "badgeTexture", HARD.name(),
                "rate", BigDecimal.valueOf(4.5)
            ));

            SummarizedReviewResponse response = reviewService.getSummarizedReview(
                targetBoard.getId());

            assertThat(response.getBadges()).isEqualTo(
                List.of(GOOD.name(), SWEET.name(), SOFT.name()));
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

            SummarizedReviewResponse response = reviewService.getSummarizedReview(
                targetBoard.getId());

            assertThat(response.getCount()).isEqualTo(2);

            assertThat(response.getRating()).isEqualTo(
                Decimal.valueOf((score[0] + score[1])).divide(
                    BigDecimal.valueOf(score.length), 2, RoundingMode.DOWN));
        }
    }

}
