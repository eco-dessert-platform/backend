package com.bbangle.bbangle.boardstatistic.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.config.ranking.BoardGrade;
import com.bbangle.bbangle.config.ranking.BoardWishCount;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.boardstatistic.repository.BoardStatisticRepository;
import com.bbangle.bbangle.review.repository.ReviewRepository;
import com.bbangle.bbangle.review.service.ReviewService;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import com.bbangle.bbangle.wishlist.service.WishListBoardService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
        BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(boardId)
            .orElseThrow(
                () -> new BbangleException(BbangleErrorCode.RANKING_NOT_FOUND));
        boardStatistic.updateViewCount();
        boardStatistic.updateBasicScore(BOARD_VIEW_SCORE, true);
    }

    @Async
    @Transactional
    public void updateWishCount(Long boardId, boolean isWish) {
        BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(boardId)
            .orElseThrow(
                () -> new BbangleException(BbangleErrorCode.RANKING_NOT_FOUND));

        boardStatistic.updateWishCount(isWish);
        boardStatistic.updateBasicScore(BOARD_WISH_SCORE, isWish);
    }

    @Async
    @Transactional
    public void updateReviewCount(
        Long boardId,
        BigDecimal rate,
        boolean isCreate
    ) {
        BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(boardId)
            .orElseThrow(
                () -> new BbangleException(BbangleErrorCode.RANKING_NOT_FOUND));
        boardStatistic.updateReviewGrade(rate, isCreate);
        boardStatistic.updateReviewCount(isCreate);
        boardStatistic.updateBasicScore(BOARD_REVIEW_SCORE, isCreate);
    }

}
