package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.board.domain.RecommendationSimilarBoard;
import com.bbangle.bbangle.board.domain.SimilarityTypeEnum;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RecommendationSimilarBoardFixture {

    private static final int LIMIT_SIMILAR_BOARD_COUNT = 3;

    public static List<RecommendationSimilarBoard> getRandom(
        Long boardId,
        List<Long> recommandationItems
    ) {
        Random random = new Random();
        BigDecimal[] randomScores = new BigDecimal[LIMIT_SIMILAR_BOARD_COUNT];
        List<RecommendationSimilarBoard> recommendationSimilarBoards = new ArrayList<>(3);

        for (int i = 0; i < LIMIT_SIMILAR_BOARD_COUNT; i++) {
            randomScores[i] = BigDecimal.valueOf(random.nextDouble(0, 1));
        }

        for (int i = 0; i < LIMIT_SIMILAR_BOARD_COUNT; i++) {
            int rank = findRank(randomScores, randomScores[i]);

            RecommendationSimilarBoard recommendationSimilarBoard = RecommendationSimilarBoard.builder()
                .queryItem(boardId)
                .recommendationItem(recommandationItems.get(i))
                .score(randomScores[i])
                .rank(rank)
                .recommendationTheme(SimilarityTypeEnum.word2vec) // TODO - Enum이 3개 이상일 때 random으로 값을 가져올 수 있는 방법 고민
                .modelVersion("0.0.1")
                .build();
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

    private static void logging(RecommendationSimilarBoard recommendationSimilarBoard) {
        log.info(recommendationSimilarBoard.toString());
    }
}
