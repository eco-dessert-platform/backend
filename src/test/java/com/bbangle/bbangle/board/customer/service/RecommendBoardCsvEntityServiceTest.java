package com.bbangle.bbangle.board.customer.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.RecommendBoardConfig;
import com.bbangle.bbangle.board.domain.RedisKeyEnum;
import com.bbangle.bbangle.board.repository.RecommendAiBoardRepository;
import com.bbangle.bbangle.board.repository.RecommendationSimilarBoardMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RecommendBoardCsvEntityServiceTest extends AbstractIntegrationTest {

    @Autowired
    private RecommendAiBoardService recommendAiBoardService;

    @Autowired
    private RecommendAiBoardRepository recommendAiBoardRepository;

    @Autowired
    private RecommendationSimilarBoardMemoryRepository memoryRepository;

    @BeforeEach
    void before() {
        memoryRepository.delete(RecommendBoardConfig.create(0, 0));
    }

    @Test
    void saveRecommendBoardFile() {
        recommendAiBoardService.saveRecommendBoardEntity();
        recommendAiBoardService.saveRecommendBoardEntity();

        var result = recommendAiBoardRepository.findAll();
        var result2 = memoryRepository.findById(RedisKeyEnum.RECOMMENDATION_CONFIG)
            .orElse(null);

        assertThat(result).isNotNull()
            .isNotEmpty()
            .hasSize(100);

        assertThat(result2).isNotNull();
        assertThat(result2.getMaxProductCount()).isEqualTo(2110);
        assertThat(result2.getNextCursor()).isEqualTo(100);
    }
}
