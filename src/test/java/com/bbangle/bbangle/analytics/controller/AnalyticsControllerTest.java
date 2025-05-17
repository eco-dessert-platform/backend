package com.bbangle.bbangle.analytics.controller;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.review.domain.Badge;
import com.bbangle.bbangle.review.domain.Review;
import com.bbangle.bbangle.board.domain.Store;
import com.bbangle.bbangle.token.oauth.domain.OauthServerType;
import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AnalyticsControllerTest extends AbstractIntegrationTest {

    @Autowired
    ResponseService responseService;
    @Autowired
    PlatformTransactionManager tm;
    @Autowired
    EntityManager em;

    @BeforeEach
    void setUpMockMvc() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(
            new AnalyticsController(responseService, analyticsService)).build();

        LocalDateTime createdAt = LocalDateTime.now().minusDays(10);
        create10DaysAgoMembers(createdAt);
        List<Member> members = createMembers();
        createWishListBoards(members);
        createBoards(members);
        create10DaysAgoWishlistBoards(createdAt);
        createReviews(members);
        create10DaysAgoReviews(members, createdAt);
    }

    @Test
    @DisplayName("전체 회원의 수가 정상적으로 조회된다.")
    void getMembersCount() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/members/count"))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @Test
    @DisplayName("기간 내 가입한 회원의 수가 정상적으로 조회된다.")
    void getNewMembersCount() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/new-members/count"))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @Test
    @DisplayName("기간 내 날짜 별 생성된 위시리스트 수, 총 데이터 수와 평균 값이 정상적으로 조회된다.")
    void getWishlistBoardAnalytics() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/wishlistboards?startDate=2024-06-01&endDate=2024-06-08"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("기간 내 날짜 별 생성된 리뷰 수, 총 데이터 수와 평균 값이 정상적으로 조회된다.")
    void getReviewAnalytics() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/reviews?startDate=2024-06-01&endDate=2024-06-08"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("기간 내 날짜 별 누적된 리뷰 수가 정상적으로 조회된다.")
    void getAccumulatedReviewsCount() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/accumulated-reviews/count?startDate=2024-06-01&endDate=2024-06-08"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    private void create10DaysAgoMembers(LocalDateTime createdAt) {
        TransactionStatus status = null;

        try {
            DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
            transactionDefinition.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRED);
            status = tm.getTransaction(transactionDefinition);

            for (int i = 1; i <= 10; i++) {
                Member member = Member.builder()
                    .email("test" + i + "@email.com")
                    .name("testUser" + i)
                    .provider(OauthServerType.KAKAO)
                    .isDeleted(false)
                    .build();

                memberRepository.save(member);
                em.flush();

                Query query = em.createQuery(
                    "UPDATE Member as m SET m.createdAt = :createdAt WHERE m.id = :id");
                query.setParameter("createdAt", createdAt);
                query.setParameter("id", member.getId());
                query.executeUpdate();
            }

            tm.commit(status);
        } catch (Exception e) {
            if (status != null) {
                tm.rollback(status);
            }
        }
    }

    private List<Member> createMembers() {
        List<Member> members = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            Member member = Member.builder()
                .email("test" + i + "@email.com")
                .name("testUser" + i)
                .provider(OauthServerType.KAKAO)
                .isDeleted(false)
                .build();

            members.add(member);
        }

        return memberRepository.saveAll(members);
    }

    private void createWishListBoards(List<Member> members) {
        for (int i = 0; i < members.size(); i++) {
            Store store = Store.builder()
                .id((long) i + 1)
                .name("test" + i)
                .introduce("introduce" + i)
                .isDeleted(false)
                .build();
            storeRepository.save(store);

            Board board = Board.builder()
                .id((long) i + 1)
                .store(store)
                .title("title" + i)
                .build();
            boardRepository.save(board);

            WishListBoard wishListBoard = WishListBoard.builder()
                .id((long) i + 1)
                .memberId(members.get(i).getId())
                .boardId(board.getId())
                .build();
            wishListBoardRepository.save(wishListBoard);
        }
    }

    private void createBoards(List<Member> members) {
        for (int i = 1; i <= members.size(); i++) {
            Store store = Store.builder()
                .id((long) i + 1)
                .name("test" + i)
                .introduce("introduce" + i)
                .isDeleted(false)
                .build();
            storeRepository.save(store);

            Board board = Board.builder()
                .id((long) i + 1)
                .store(store)
                .title("title" + i)
                .build();
            boardRepository.save(board);
        }
    }

    private void create10DaysAgoWishlistBoards(LocalDateTime createdAt) {
        TransactionStatus status = null;

        try {
            DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
            transactionDefinition.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRED);
            status = tm.getTransaction(transactionDefinition);

            for (int i = 1; i <= 10; i++) {
                WishListBoard wishListBoard = WishListBoard.builder()
                    .build();

                wishListBoardRepository.save(wishListBoard);
                em.flush();

                Query query = em.createQuery(
                    "UPDATE WishListBoard as wb SET wb.createdAt = :createdAt WHERE wb.id = :id");
                query.setParameter("createdAt", createdAt);
                query.setParameter("id", wishListBoard.getId());
                query.executeUpdate();
            }

            tm.commit(status);
        } catch (Exception e) {
            if (status != null) {
                tm.rollback(status);
            }
        }
    }

    private void createReviews(List<Member> members) {
        for (int i = 1; i <= members.size(); i++) {
            Review review = Review.builder()
                .memberId(members.get(i - 1).getId())
                .boardId((long) i)
                .badgeBrix(Badge.SWEET)
                .badgeTaste(Badge.GOOD)
                .badgeTexture(Badge.DRY)
                .rate(BigDecimal.valueOf(5))
                .build();

            reviewRepository.save(review);
        }
    }

    private void create10DaysAgoReviews(List<Member> members, LocalDateTime createdAt) {
        TransactionStatus status = null;

        try {
            DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
            transactionDefinition.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRED);
            status = tm.getTransaction(transactionDefinition);

            for (int i = 1; i <= 10; i++) {
                Review review = Review.builder()
                    .memberId(members.get(i - 1).getId())
                    .boardId((long) i + 20)
                    .badgeBrix(Badge.SWEET)
                    .badgeTaste(Badge.GOOD)
                    .badgeTexture(Badge.DRY)
                    .rate(BigDecimal.valueOf(5))
                    .build();

                reviewRepository.save(review);
                em.flush();

                Query query = em.createQuery(
                    "UPDATE Review as r SET r.createdAt = :createdAt WHERE r.id = :id");
                query.setParameter("createdAt", createdAt);
                query.setParameter("id", review.getId());
                query.executeUpdate();
            }

            tm.commit(status);
        } catch (Exception e) {
            if (status != null) {
                tm.rollback(status);
            }
        }
    }

}
