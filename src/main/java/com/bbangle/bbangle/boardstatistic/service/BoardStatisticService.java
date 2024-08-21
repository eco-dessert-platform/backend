package com.bbangle.bbangle.boardstatistic.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.boardstatistic.repository.BoardPreferenceStatisticRepository;
import com.bbangle.bbangle.boardstatistic.update.StatisticUpdate;
import com.bbangle.bbangle.boardstatistic.ranking.BoardGrade;
import com.bbangle.bbangle.boardstatistic.ranking.BoardWishCount;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.boardstatistic.repository.BoardStatisticRepository;
import com.bbangle.bbangle.review.dao.ReviewStatisticDao;
import com.bbangle.bbangle.review.repository.ReviewRepository;
import com.bbangle.bbangle.wishlist.dao.WishListStatisticDao;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardStatisticService {

    private static final String STATISTIC_UPDATE_LIST = "STATISTIC_UPDATE_LIST";

    private final BoardStatisticRepository boardStatisticRepository;
    private final BoardPreferenceStatisticRepository preferenceStatisticRepository;
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
                .boardReviewCount(0L)
                .boardWishCount(0L)
                .boardViewCount(0L)
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
        for (BoardStatistic statistic : allStatistics) {
            statistic.updateBasicScoreWhenInit();
        }
    }

    private void updatingBoardReviewCountAndRate(
        List<BoardGrade> reviewGradeList,
        List<BoardStatistic> allStatistics
    ) {
        for (BoardGrade boardGrade : reviewGradeList) {
            for (BoardStatistic statistic : allStatistics) {
                if (boardGrade.boardId()
                    .equals(statistic.getBoardId())) {
                    statistic.setBoardReviewCountWhenInit((long) boardGrade.count());
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
                if (wishCount.boardId()
                    .equals(statistic.getBoardId())) {
                    statistic.setBoardWishCountWhenInit((long) wishCount.count());
                }
            }
        }
    }

    @Async
    @Transactional
    public void updateViewCount(Long boardId) {
        StatisticUpdate boardViewUpdate = StatisticUpdate.updateViewCount(boardId);
        redisTemplate.opsForList()
            .rightPush(STATISTIC_UPDATE_LIST, boardViewUpdate);
    }

    @Async
    @Transactional
    public void updateWishCount(Long boardId, boolean isWish) {
        StatisticUpdate boardWishUpdate = StatisticUpdate.updateWishCount(boardId, isWish);
        redisTemplate.opsForList()
            .rightPush(STATISTIC_UPDATE_LIST, boardWishUpdate);
    }

    @Async
    @Transactional
    public void updateReview(
        Long boardId
    ) {
        StatisticUpdate ReviewRateUpdate = StatisticUpdate.updateReview(boardId);
        redisTemplate.opsForList()
            .rightPush(STATISTIC_UPDATE_LIST, ReviewRateUpdate);
    }

    @Transactional
    public void updateInBatch(
        List<Long> boardWishUpdateId,
        List<Long> boardReviewUpdateId,
        Map<Long, Integer> boardViewCountUpdate,
        List<Long> allUpdateBoard
    ) {
        List<BoardStatistic> updateList = boardStatisticRepository.findAllByBoardIds(
            allUpdateBoard);
        boardViewCountUpdate.keySet()
            .forEach(key -> {
                for (BoardStatistic update : updateList) {
                    if (update.getBoardId()
                        .equals(key)) {
                        update.updateViewCount(boardViewCountUpdate.get(key));
                    }
                }
            });
        List<ReviewStatisticDao> reviewStatisticByBoardIds = reviewRepository.getReviewStatisticByBoardIds(
            boardReviewUpdateId);
        List<WishListStatisticDao> wishStatisticByBoardIds = wishListBoardRepository.findWishStatisticByBoardIds(
            boardWishUpdateId);

        for (BoardStatistic statistic : updateList) {
            for (ReviewStatisticDao reviewStatisticDao : reviewStatisticByBoardIds) {
                if (statistic.getBoardId()
                    .equals(reviewStatisticDao.boardId())) {
                    statistic.updateReviewGrade(
                        BigDecimal.valueOf(reviewStatisticDao.averageRate()));
                    statistic.updateReviewCount(reviewStatisticDao.reviewCount());
                }
            }

            for (WishListStatisticDao dao : wishStatisticByBoardIds) {
                if(statistic.getBoardId().equals(dao.boardId())) {
                    statistic.updateWishCount(dao.wishListCount());
                }
            }

        }
    }

}

