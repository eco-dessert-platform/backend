package com.bbangle.bbangle.ranking.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.ranking.domain.Ranking;
import com.bbangle.bbangle.ranking.repository.RankingRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;
    private final BoardRepository boardRepository;

    public void updatingNonRankedBoards() {
        List<Board> unRankedBoards = boardRepository.checkingNullRanking();
        List<Ranking> rankings = new ArrayList<>();
        unRankedBoards.stream()
            .map(board -> Ranking.builder()
                .board(board)
                .popularScore(0.0)
                .recommendScore(0.0)
                .build())
            .forEach(rankings::add);
        rankingRepository.saveAll(rankings);
    }

    @Transactional
    public void updateRankingScore(Long boardId, Double updatingScore) {
        Ranking ranking = rankingRepository.findByBoardId(boardId)
            .orElseThrow(
                () -> new BbangleException(BbangleErrorCode.RANKING_NOT_FOUND));
        ranking.updateRecommendScore(updatingScore);
    }

}
