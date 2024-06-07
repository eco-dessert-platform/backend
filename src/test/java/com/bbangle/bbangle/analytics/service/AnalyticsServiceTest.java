package com.bbangle.bbangle.analytics.service;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.analytics.dto.AnalyticsCountWithDateResponseDto;
import com.bbangle.bbangle.analytics.dto.AnalyticsRatioWithDateResponseDto;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.common.domain.Badge;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.review.domain.Review;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.token.oauth.domain.OauthServerType;
import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyticsServiceTest extends AbstractIntegrationTest {

    @Autowired PlatformTransactionManager tm;
    @Autowired EntityManager em;


    @Test
    @DisplayName("전체 회원의 수가 정상적으로 조회된다.")
    void countAllMember() {
        // given
        LocalDateTime createdAt = LocalDateTime.now().minusDays(10);
        create10DaysAgoMembers(createdAt);
        createMembers();

        // when
        long result = analyticsService.countAllMembers().count();

        // then
        assertThat(result).isEqualTo(20);
    }


    @Test
    @DisplayName("기간 내 가입한 회원의 수가 정상적으로 조회된다.")
    void countNewMember() {
        // given
        LocalDateTime createdAt = LocalDateTime.now().minusDays(10);
        create10DaysAgoMembers(createdAt);
        createMembers();

        Optional<LocalDate> startDate = Optional.of(LocalDate.now().minusDays(9));
        Optional<LocalDate> endDate = Optional.of(LocalDate.now());

        // then
        long membersCount = memberRepository.count();
        List<AnalyticsCountWithDateResponseDto> result = analyticsService.countMembersByPeriod(startDate, endDate);

        // then
        assertThat(membersCount).isEqualTo(20);
        assertThat(result).hasSize(10);
    }


    @Test
    @DisplayName("기간 내 일별 위시리스트 이용 비율이 성공적으로 조회된다.")
    void countMembersUsingWishlist() {
        // given
        LocalDateTime createdAt = LocalDateTime.now().minusDays(10);
        List<Member> members1 = create10DaysAgoMembers(createdAt);
        createMembers();
        createMembers();
        createWishListBoards(members1);
        create10DaysAgoWishlistBoards(members1, createdAt);

        Optional<LocalDate> optStartDate = Optional.of(LocalDate.now().minusDays(10));
        Optional<LocalDate> optEndDate = Optional.of(LocalDate.now());
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        // when
        long membersCount = memberRepository.count();
        List<AnalyticsCountWithDateResponseDto> countWithDateResponseDtos = wishListBoardRepository.countMembersUsingWishlistBetweenPeriod(startDate, endDate);
        List<AnalyticsRatioWithDateResponseDto> results = analyticsService.calculateWishlistUsingRatio(optStartDate, optEndDate);

        // then
        assertThat(membersCount).isEqualTo(30);
        assertThat(countWithDateResponseDtos).hasSize(11);
        assertThat(results).hasSize(11);
        assertThat(results.get(0).ratio()).isEqualTo("100.00");
        assertThat(results.get(9).ratio()).isEqualTo("0.00");
        assertThat(results.get(10).ratio()).isEqualTo("33.33");
    }



    @Test
    @DisplayName("기간 별 위시리스트 누적 개수가 정상적으로 조회된다.")
    void countWishlistBoardByPeriod() {
        // given
        List<Member> members = createMembers();
        createWishListBoards(members);

        LocalDateTime createdAt = LocalDateTime.now().minusDays(10);
        create10DaysAgoWishlistBoards(members, createdAt);

        // when
        Optional<LocalDate> startDate = Optional.of(LocalDate.now().minusDays(9));
        Optional<LocalDate> endDate = Optional.of(LocalDate.now());
        Long wishlistBoardsCount = wishListBoardRepository.count();
        List<AnalyticsCountWithDateResponseDto> results = analyticsService.countWishlistBoardByPeriod(startDate, endDate);

        // then
        assertThat(wishlistBoardsCount).isEqualTo(20);
        assertThat(results).hasSize(10);
        assertThat(results.get(0).count()).isEqualTo(10);
        assertThat(results.get(8).count()).isEqualTo(10);
        assertThat(results.get(9).count()).isEqualTo(20);
    }


    @Test
    @DisplayName("기간 내 일별 리뷰 이용 비율이 정상적으로 조회된다.")
    void calculateReviewUsingRatio() {
        // given
        LocalDateTime createdAt = LocalDateTime.now().minusDays(10);
        List<Member> members1 = create10DaysAgoMembers(createdAt);
        createMembers();
        createMembers();
        createMembers();
        createReviews(members1);
        create10DaysAgoReviews(members1, createdAt);

        Optional<LocalDate> optStartDate = Optional.of(LocalDate.now().minusDays(10));
        Optional<LocalDate> optEndDate = Optional.of(LocalDate.now());
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        // when
        long membersCount = memberRepository.count();
        List<AnalyticsCountWithDateResponseDto> analyticsCountWithDateResponseDtos = reviewRepository.countMembersUsingReviewBetweenPeriod(startDate, endDate);
        List<AnalyticsRatioWithDateResponseDto> results = analyticsService.calculateReviewUsingRatio(optStartDate, optEndDate);

        // then
        assertThat(membersCount).isEqualTo(40);
        assertThat(analyticsCountWithDateResponseDtos).hasSize(11);
        assertThat(results).hasSize(11);
        assertThat(results.get(0).ratio()).isEqualTo("100.00");
        assertThat(results.get(9).ratio()).isEqualTo("0.00");
        assertThat(results.get(10).ratio()).isEqualTo("25.00");
    }


    @Test
    @DisplayName("기간 별 리뷰 누적 개수가 정상적으로 조회된다.")
    void countReviewByPeriod() {
        // given
        LocalDateTime createdAt = LocalDateTime.now().minusDays(10);
        List<Member> members1 = create10DaysAgoMembers(createdAt);
        createReviews(members1);
        create10DaysAgoReviews(members1, createdAt);

        // when
        LocalDate startDate = LocalDate.now().minusDays(9);
        LocalDate endDate = LocalDate.now();
        long reviewsCount = reviewRepository.count();
        List<AnalyticsCountWithDateResponseDto> results = reviewRepository.countReviewCreatedBetweenPeriod(startDate, endDate);

        // then
        assertThat(reviewsCount).isEqualTo(20);
        assertThat(results).hasSize(10);
        assertThat(results.get(0).count()).isEqualTo(10);
        assertThat(results.get(8).count()).isEqualTo(10);
        assertThat(results.get(9).count()).isEqualTo(20);
    }


    private List<Member> create10DaysAgoMembers(LocalDateTime createdAt) {
        TransactionStatus status = null;

        try {
            DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
            transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
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

                Query query = em.createQuery("UPDATE Member as m SET m.createdAt = :createdAt WHERE m.id = :id");
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

        return memberRepository.findAll();
    }


    private List<Member> createMembers() {
        List<Member> members = new ArrayList<>();

        for(int i = 1; i <= 10; i++){
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


    private void create10DaysAgoWishlistBoards(List<Member> members, LocalDateTime createdAt) {
        TransactionStatus status = null;

        try {
            DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
            transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            status = tm.getTransaction(transactionDefinition);

            for (int i = 0; i < 10; i++) {
                WishListBoard wishListBoard = WishListBoard.builder()
                        .memberId(members.get(i).getId())
                        .build();

                wishListBoardRepository.save(wishListBoard);
                em.flush();

                Query query = em.createQuery("UPDATE WishListBoard as wb SET wb.createdAt = :createdAt WHERE wb.id = :id");
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
                    .badgeBrix(String.valueOf(Badge.SWEET))
                    .badgeTaste(String.valueOf(Badge.GOOD))
                    .badgeTexture(String.valueOf(Badge.HARD))
                    .rate(BigDecimal.valueOf(5))
                    .build();

            reviewRepository.save(review);
        }
    }


    private void create10DaysAgoReviews(List<Member> members, LocalDateTime createdAt) {
        TransactionStatus status = null;

        try {
            DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
            transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            status = tm.getTransaction(transactionDefinition);

            for (int i = 1; i <= 10; i++) {
                Review review = Review.builder()
                        .memberId(members.get(i - 1).getId())
                        .boardId((long) i + 20)
                        .badgeBrix(String.valueOf(Badge.SWEET))
                        .badgeTaste(String.valueOf(Badge.GOOD))
                        .badgeTexture(String.valueOf(Badge.HARD))
                        .rate(BigDecimal.valueOf(5))
                        .build();

                reviewRepository.save(review);
                em.flush();

                Query query = em.createQuery("UPDATE Review as r SET r.createdAt = :createdAt WHERE r.id = :id");
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