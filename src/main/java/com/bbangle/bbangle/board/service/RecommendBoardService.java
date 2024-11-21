package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.domain.RecommendationSimilarBoard;
import com.bbangle.bbangle.board.repository.RecommendBoardRepository;
import com.bbangle.bbangle.board.service.csv.RecommendBoardConfigCsvEntity;
import com.bbangle.bbangle.board.service.csv.RecommendBoardConfigPipeLine;
import com.bbangle.bbangle.board.service.csv.RecommendBoardCsvEntity;
import com.bbangle.bbangle.board.service.csv.RecommendBoardPipeLine;
import com.bbangle.bbangle.board.util.RecommendCursorRange;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendBoardService {

    private static final boolean SCHEDULING_CONTINUE = true;
    private static final boolean SCHEDULING_STOP = false;
    private final RecommendBoardRepository recommendBoardRepository;
    private final RecommendBoardFileStorageService fileStorageService;

    public boolean saveRecommendBoardEntity() {
        List<List<String>> configFile = fileStorageService.readRecommendationConfigCsvFile();
        RecommendBoardConfigPipeLine configPipeLine = new RecommendBoardConfigPipeLine(configFile);
        RecommendBoardConfigCsvEntity configCsvEntity = configPipeLine.mapToCsvEntity();

        RecommendCursorRange cursorRange = RecommendCursorRange.builder()
            .maxProductCount(configCsvEntity.getMaxProductCount())
            .nextCursor(configCsvEntity.getNextCursor())
            .build();

        if (cursorRange.getStartCursor() == cursorRange.getEndCursor()) {
            return SCHEDULING_CONTINUE;
        }

        List<RecommendationSimilarBoard> recommendationSimilarBoards = loadRecommendationCsvFile(
            cursorRange.getStartCursor(), cursorRange.getEndCursor());

        recommendBoardRepository.saveAll(recommendationSimilarBoards);

        uploadConfigFile(configCsvEntity, configPipeLine, cursorRange);

        return SCHEDULING_STOP;
    }

    private List<RecommendationSimilarBoard> loadRecommendationCsvFile(int startCursor,
        int endCursor) {
        List<List<String>> recommendationData = fileStorageService.readRecommendationCsvFile(
            startCursor, endCursor);
        RecommendBoardPipeLine pipeLine = new RecommendBoardPipeLine(recommendationData);
        List<RecommendBoardCsvEntity> recommendBoardCsvEntities = pipeLine.mapToCsvEntity();
        return pipeLine.mapToEntity(
            recommendBoardCsvEntities);
    }

    private void uploadConfigFile(
        RecommendBoardConfigCsvEntity configCsvEntity,
        RecommendBoardConfigPipeLine configPipeLine,
        RecommendCursorRange cursorRange
    ) {
        configCsvEntity.updateNextCursor(cursorRange.getEndCursor());
        List<List<Object>> configCsvData = configPipeLine.mapToList(configCsvEntity);
        fileStorageService.uploadRecommendationConfigCsvFile(configCsvData);
    }
}
