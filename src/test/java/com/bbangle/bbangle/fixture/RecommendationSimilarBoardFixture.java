package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.RecommendationSimilarBoard;
import com.bbangle.bbangle.board.domain.SimilarityTypeEnum;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.logging.Log;

import java.math.BigDecimal;
import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RecommendationSimilarBoardFixture {

    private static final int LIMIT_SIMILAR_BOARD_COUNT = 3;
    private static Log log;

    public static List<RecommendationSimilarBoard> getRandom(
        Long boardId,
        List<Long> recommendationItems
    ) {
        Random random = new Random();
        BigDecimal[] randomScores = new BigDecimal[LIMIT_SIMILAR_BOARD_COUNT];
        List<RecommendationSimilarBoard> recommendationSimilarBoards = new ArrayList<>(3);

        for (int i = 0; i < LIMIT_SIMILAR_BOARD_COUNT; i++) {
            randomScores[i] = BigDecimal.valueOf(random.nextDouble(0, 1));
        }

        for (int i = 0; i < LIMIT_SIMILAR_BOARD_COUNT; i++) {
            int rank = findRank(randomScores, randomScores[i]);

            RecommendationSimilarBoard recommendationSimilarBoard = RecommendationSimilarBoard.create(
                boardId,
                rank,
                recommendationItems.get(i),
                randomScores[i],
                SimilarityTypeEnum.word2vec,
                "0.0.1"
            );

            recommendationSimilarBoards.add(recommendationSimilarBoard);
            logging(recommendationSimilarBoard);
        }

        return recommendationSimilarBoards;
    }

    private static int findRank(BigDecimal[] scores, BigDecimal targetScore) {
        BigDecimal[] sortedScores = scores.clone();
        Arrays.sort(sortedScores, Comparator.reverseOrder());

        // 각 점수의 첫 번째 등장 순위 저장
        Map<BigDecimal, Integer> rankMap = new HashMap<>();
        for (int j = 0; j < sortedScores.length; j++) {
            rankMap.putIfAbsent(sortedScores[j], j + 1);
        }

        // 대상 점수의 순위 반환
        return rankMap.get(targetScore);
    }

    public static RecommendationSimilarBoard getRandomSingleEntity(
        Board targetBoard,
        Board recommandationBoard,
        int rank
    ) {
        Random random = new Random();
        BigDecimal randomScore = BigDecimal.valueOf(random.nextDouble(0, 1));

        return RecommendationSimilarBoard.create(
            targetBoard.getId(),
            rank,
            recommandationBoard.getId(),
            randomScore,
            SimilarityTypeEnum.word2vec,
            "0.0.1");
    }

    private static void logging(RecommendationSimilarBoard recommendationSimilarBoard) {
        log.info(recommendationSimilarBoard.toString());
    }
}
