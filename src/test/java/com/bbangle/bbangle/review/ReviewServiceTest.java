package com.bbangle.bbangle.review;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.ranking.domain.BoardStatistic;
import com.bbangle.bbangle.review.domain.Badge;
import com.bbangle.bbangle.review.domain.Review;
import com.bbangle.bbangle.review.domain.ReviewImg;
import com.bbangle.bbangle.review.dto.ReviewRequest;
import com.bbangle.bbangle.review.repository.ReviewImgRepository;
import com.bbangle.bbangle.review.repository.ReviewRepository;
import com.bbangle.bbangle.review.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.bbangle.bbangle.review.domain.Badge.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        List<Member> member = memberRepository.findAll();

        //when
        reviewService.makeReview(reviewRequest, member.get(0).getId());
        List<Review> reviewList = reviewRepository.findAll();
        List<ReviewImg> reviewImg = reviewImgRepository.findAll();

        //then
        assertThat(reviewList).hasSize(1);
        assertThat(reviewList.get(0).getBadgeTaste()).isEqualTo(GOOD);
        assertThat(reviewList.get(0).getBadgeBrix()).isEqualTo(PLAIN);
        assertThat(reviewList.get(0).getBadgeTexture()).isEqualTo(SOFT);
        //TODO: 현재 이미지를 저장하고 있지 않음
//        assertThat(reviewImg).hasSize(1);
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
        List<String> photos = new ArrayList<>();
        photos.add("test");
        return new ReviewRequest(badges, new BigDecimal("4.0"), null,
            board.get(0).getId(), photos);
    }
}
