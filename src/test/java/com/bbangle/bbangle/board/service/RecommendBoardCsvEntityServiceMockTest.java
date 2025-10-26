package com.bbangle.bbangle.board.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.customer.service.RecommendAiBoardService;
import com.bbangle.bbangle.board.customer.service.RecommendBoardScheduler;
import com.bbangle.bbangle.board.customer.service.component.RecommendBoardFileStorageComponent;
import com.bbangle.bbangle.board.domain.RecommendBoardConfig;
import com.bbangle.bbangle.board.domain.RedisKeyEnum;
import com.bbangle.bbangle.board.repository.RecommendationSimilarBoardMemoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

class RecommendBoardCsvEntityServiceMockTest extends AbstractIntegrationTest {

    @MockBean
    private RecommendationSimilarBoardMemoryRepository memoryRepository;

    @MockBean
    private RecommendBoardFileStorageComponent fileStorageService;

    @Autowired
    private RecommendAiBoardService recommendAiBoardService;

    @Autowired
    private RecommendBoardScheduler recommendBoardScheduler;

    @AfterEach
    void after() {
        Mockito.reset(memoryRepository, fileStorageService);
        memoryRepository.delete(RecommendBoardConfig.create(0, 0));
    }

    @BeforeEach
    void before() {
        memoryRepository.delete(RecommendBoardConfig.create(0, 0));
    }

    @Test
    void stopScheduling() {
        RecommendBoardConfig mockConfig = RecommendBoardConfig.create(0, 0);

        when(memoryRepository.findById(RedisKeyEnum.RECOMMENDATION_CONFIG))
            .thenReturn(java.util.Optional.of(mockConfig));

        var result = recommendAiBoardService.saveRecommendBoardEntity();

        assertThat(result).isEqualTo(false);
    }

    @Test
    void throwError() {
        when(fileStorageService.readRecommendationCsvFile(Mockito.anyInt(), Mockito.anyInt()))
            .thenThrow(new RuntimeException("잘못된 CSV 파일 형식"));

        assertThatThrownBy(() -> recommendBoardScheduler.scheduleRecommendBoardUpdate())
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("잘못된 CSV 파일 형식");
    }
}
