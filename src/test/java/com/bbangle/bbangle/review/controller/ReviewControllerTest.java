package com.bbangle.bbangle.review.controller;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.mock.WithCustomMockUser;
import com.bbangle.bbangle.review.domain.Badge;
import com.bbangle.bbangle.review.domain.Review;
import com.bbangle.bbangle.review.domain.ReviewImg;
import com.bbangle.bbangle.review.domain.ReviewLike;
import com.bbangle.bbangle.review.repository.ReviewImgRepository;
import com.bbangle.bbangle.review.repository.ReviewLikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

import static java.util.Collections.emptyMap;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReviewControllerTest extends AbstractIntegrationTest {
    private static final String TASTE_REACTION = "맛있어요";
    private static final String BASE_PATH = "/api/v1/review/";
    @Autowired
    private ReviewLikeRepository reviewLikeRepository;
    @Autowired
    private ReviewImgRepository reviewImgRepository;

    @BeforeEach
    public void setUp() {
        Board board = boardRepository.save(fixtureBoard(emptyMap()));
        productRepository.save(ProductFixture.randomProduct(board));
        boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board));
        Long boardId = board.getId();
        Member kakaoMember = MemberFixture.createKakaoMember();
        Member savedMember = memberRepository.save(kakaoMember);
        Long memberId = savedMember.getId();

        Review review = Review.builder()
                .boardId(boardId)
                .badgeBrix(Badge.SWEET)
                .badgeTaste(Badge.GOOD)
                .badgeTexture(Badge.DRY)
                .content(TASTE_REACTION)
                .rate(new BigDecimal("4.5"))
                .memberId(memberId)
                .build();
        Review savedReview = reviewRepository.save(review);

        ReviewLike reviewLike = ReviewLike.builder()
                .reviewId(savedReview.getId())
                .memberId(memberId)
                .build();
        reviewLikeRepository.save(reviewLike);

        ReviewImg reviewImg = ReviewImg.builder()
                .reviewId(savedReview.getId())
                .url("testImage")
                .build();
        reviewImgRepository.save(reviewImg);
    }

    @Test
    @DisplayName("도움돼요, 리뷰 이미지, 리뷰를 삭제한다")
    @WithCustomMockUser
    void deleteReview() throws Exception {
        Long reviewId = reviewRepository.findAll().stream().findFirst().get().getId();
        mockMvc.perform(delete(BASE_PATH+reviewId))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("게시물에 있는 리뷰의 평점과 개수를 조회한다")
    void getReviewRate() throws Exception{
        Long boardId = boardRepository.findAll().stream().findFirst().get().getId();
        mockMvc.perform(get("/api/v1/review/rate/"+boardId))
                .andExpect(jsonPath("$.result.rating").value("4.5"))
                .andExpect(jsonPath("$.result.count").value(1))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("게시물에 있는 리뷰 목록을 조회한다")
    void getReviews() throws Exception {
        Long boardId = boardRepository.findAll().stream().findFirst().get().getId();
        mockMvc.perform(get("/api/v1/review/list/"+boardId))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("도움돼요를 추가한다")
    @WithCustomMockUser
    void insertLike() throws Exception {
        Long reviewId = reviewRepository.findAll().stream().findFirst().get().getId();
        mockMvc.perform(get("/api/v1/review/like/"+reviewId))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("도움돼요를 삭제한다")
    @WithCustomMockUser
    void removeLike() throws Exception {
        Long reviewId = reviewRepository.findAll().stream().findFirst().get().getId();
        mockMvc.perform(delete("/api/v1/review/like/"+reviewId))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("리뷰 상세 정보를 조회한다")
    @WithCustomMockUser
    void getReviewDetail() throws Exception {
        Long reviewId = reviewRepository.findAll().stream().findFirst().get().getId();
        mockMvc.perform(get(BASE_PATH + reviewId))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("리뷰 대표 이미지를 조회한다")
    void getReviewImages() throws Exception{
        Long reviewId = reviewRepository.findAll().stream().findFirst().get().getId();
        mockMvc.perform(get(BASE_PATH+"/image/"+reviewId))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("내가 작성한 리뷰를 조회한다")
    @WithCustomMockUser
    void getMyReviews() throws Exception {
        mockMvc.perform(get(BASE_PATH+"/myreview"))
                .andExpect(status().isOk())
                .andDo(print());

    }

    @Test
    @DisplayName("리뷰의 전체 사진을 조회한다")
    void getAllImagesByBoardId() throws Exception {
        Long boardId = boardRepository.findAll().stream().findFirst().get().getId();
        mockMvc.perform(get(BASE_PATH+boardId+"/images"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("리뷰의 사진을 크게 본다")
    void getImage() throws Exception{
        Long reviewImgId = reviewImgRepository.findAll().stream().findFirst().get().getId();
        mockMvc.perform(get(BASE_PATH+"/images/"+reviewImgId))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("리뷰의 사진을 삭제한다")
    @WithCustomMockUser
    void deleteImage() throws Exception{
        Long reviewImgId = reviewImgRepository.findAll().stream().findFirst().get().getId();
        mockMvc.perform(delete(BASE_PATH+"/image/"+reviewImgId))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
