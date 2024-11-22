package com.bbangle.bbangle.board.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.bbangle.bbangle.board.repository.RecommendBoardRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class RecommendBoardCsvEntityServiceTest {

    @Mock
    private RecommendBoardFileStorageService fileStorageService;

    @InjectMocks
    private RecommendBoardService recommendBoardService;

    public RecommendBoardCsvEntityServiceTest() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void saveRecommendBoardFile() {

        List<List<String>> configData = new ArrayList<>();
        configData.add(List.of("MAX_PRODUCT_COUNT", "NEXT_CURSOR"));
        configData.add(List.of("10", "10"));

        List<List<String>> recommendData = new ArrayList<>();
        recommendData.add(new ArrayList<>(List.of("query_item", "recommendation_item", "score", "rank", "recommendation_theme", "model_version")));
        recommendData.add(new ArrayList<>(List.of("10", "10", "0.11111", "3", "DEFAULT", "DEFAULT")));

        when(fileStorageService.readRecommendationConfigCsvFile()).thenReturn(configData);
        when(fileStorageService.readRecommendationCsvFile(1, 2)).thenReturn(recommendData);
        doNothing().when(fileStorageService).uploadRecommendationConfigCsvFile(any());

        boolean result = recommendBoardService.saveRecommendBoardEntity();
        assertThat(result).isTrue();
    }
}
