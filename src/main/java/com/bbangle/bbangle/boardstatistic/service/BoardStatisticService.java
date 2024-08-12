package com.bbangle.bbangle.boardstatistic.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.boardstatistic.update.StatisticUpdate;
import com.bbangle.bbangle.config.ranking.BoardGrade;
import com.bbangle.bbangle.config.ranking.BoardWishCount;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.boardstatistic.repository.BoardStatisticRepository;
import com.bbangle.bbangle.review.repository.ReviewRepository;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardStatisticService {

    private static final Double BOARD_WISH_SCORE = 50.0;
    private static final Double BOARD_VIEW_SCORE = 1.0;
    private static final Double BOARD_REVIEW_SCORE = 50.0;

    private final BoardStatisticRepository boardStatisticRepository;
    private final BoardRepository boardRepository;
    private final ReviewRepository reviewRepository;
    private final WishListBoardRepository wishListBoardRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public void updatingNonRankedBoards() {
        List<Board> unRankedBoards = boardRepository.checkingNullRanking();
        List<BoardStatistic> boardStatistics = new ArrayList<>();
        unRankedBoards.stream()
            .map(board -> BoardStatistic.builder()
                .boardId(board.getId())
                .basicScore(0.0)
                .boardReviewCount(0)
                .boardWishCount(0)
                .boardViewCount(0)
                .boardReviewGrade(BigDecimal.ZERO)
                .build())
            .forEach(boardStatistics::add);
        boardStatisticRepository.saveAll(boardStatistics);
    }

    @Transactional
    public void checkingStatisticStatusAndUpdate() {
        List<BoardStatistic> allStatistics = boardStatisticRepository.findAll();
        List<BoardWishCount> wishCountList = wishListBoardRepository.groupByBoardIdAndGetWishCount();
        List<BoardGrade> reviewGradeList = reviewRepository.groupByBoardIdAndGetReviewCountAndReviewRate();
        updatingBoardWishCount(wishCountList, allStatistics);
        updatingBoardReviewCountAndRate(reviewGradeList, allStatistics);
        for(BoardStatistic statistic : allStatistics){
            statistic.updateBasicScoreWhenInit();
        }
    }

    private void updatingBoardReviewCountAndRate(
        List<BoardGrade> reviewGradeList,
        List<BoardStatistic> allStatistics
    ) {
        for (BoardGrade boardGrade : reviewGradeList) {
            for (BoardStatistic statistic : allStatistics) {
                if (boardGrade.boardId().equals(statistic.getBoardId())) {
                    statistic.setBoardReviewCountWhenInit(boardGrade.count());
                    statistic.setBoardReviewRateWhenInit(boardGrade.grade());
                }
            }
        }
    }

    private static void updatingBoardWishCount(
        List<BoardWishCount> wishCountList,
        List<BoardStatistic> allStatistics
    ) {
        for (BoardWishCount wishCount : wishCountList) {
            for (BoardStatistic statistic : allStatistics) {
                if (wishCount.boardId().equals(statistic.getBoardId())) {
                    statistic.setBoardWishCountWhenInit(wishCount.count());
                }
            }
        }
    }

    @Async
    @Transactional
    public void updateViewCount(Long boardId) {
        StatisticUpdate boardViewUpdate = StatisticUpdate.updateViewCount(boardId);
        redisTemplate.opsForList().rightPush(String.valueOf(boardId), boardViewUpdate);
    }

    @Async
    @Transactional
    public void updateWishCount(Long boardId, boolean isWish) {
        StatisticUpdate boardWishUpdate = StatisticUpdate.updateWishCount(boardId, isWish);
        redisTemplate.opsForList().rightPush(String.valueOf(boardId), boardWishUpdate);
    }

    @Async
    @Transactional
    public void updateReviewCount(
        Long boardId,
        BigDecimal rate,
        boolean isCreate
    ) {
        StatisticUpdate ReviewRateUpdate = StatisticUpdate.updateReviewRate(boardId, rate);
        StatisticUpdate ReviewCountUpdate = StatisticUpdate.updateReviewCount(boardId);
        redisTemplate.opsForList().rightPush(String.valueOf(boardId), ReviewRateUpdate);
        redisTemplate.opsForList().rightPush(String.valueOf(boardId), ReviewCountUpdate);
    }

}
