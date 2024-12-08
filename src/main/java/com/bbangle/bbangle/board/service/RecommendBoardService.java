package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.domain.RecommendBoardConfig;
import com.bbangle.bbangle.board.domain.RecommendationSimilarBoard;
import com.bbangle.bbangle.board.domain.RedisKeyEnum;
import com.bbangle.bbangle.board.repository.RecommendBoardRepository;
import com.bbangle.bbangle.board.repository.temp.RecommendationSimilarBoardMemoryRepository;
import com.bbangle.bbangle.board.dto.RecommendBoardCsvDto;
import com.bbangle.bbangle.board.service.component.RecommendBoardFileStorageComponent;
import com.bbangle.bbangle.board.service.component.RecommendBoardMapper;
import com.bbangle.bbangle.board.util.CsvFileUtil;
import com.bbangle.bbangle.board.domain.RecommendCursorRange;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendBoardService {

    private static final boolean SCHEDULING_CONTINUE = true;
    private static final boolean SCHEDULING_STOP = false;
    private static final int MAX_PRODUCT_COUNT_NULL = -1;
    private final RecommendBoardMapper recommendBoardMapper;
    private final RecommendationSimilarBoardMemoryRepository memoryRepository;
    private final RecommendBoardRepository recommendBoardRepository;
    private final RecommendBoardFileStorageComponent fileStorageService;

    @Transactional
    public boolean saveRecommendBoardEntity() {
        RecommendBoardConfig recommendBoardConfig = memoryRepository.findById(RedisKeyEnum.RECOMMENDATION_CONFIG)
            .orElse(RecommendBoardConfig.empty());

        RecommendCursorRange cursorRange = RecommendCursorRange.builder()
            .maxProductCount(recommendBoardConfig.getMaxProductCount())
            .nextCursor(recommendBoardConfig.getNextCursor())
            .build();

        if (!recommendBoardConfig.getMaxProductCount().equals(MAX_PRODUCT_COUNT_NULL) && cursorRange.isEnd()) {
            return SCHEDULING_STOP;
        }

        List<RecommendationSimilarBoard> recommendationSimilarBoards = loadRecommendationCsvFileWithinRange(
            cursorRange);

        recommendBoardRepository.saveAll(recommendationSimilarBoards);

        initializeOrUpdateBoardConfig(recommendBoardConfig, cursorRange);

        return SCHEDULING_CONTINUE;
    }

    private void initializeOrUpdateBoardConfig(RecommendBoardConfig recommendBoardConfig,
        RecommendCursorRange cursorRange) {
        if (recommendBoardConfig.getMaxProductCount().equals(MAX_PRODUCT_COUNT_NULL)) {
            memoryRepository.save(RecommendBoardConfig.create(fileStorageService.readRowCount(), cursorRange.getEndCursor()));
            return;
        }

        recommendBoardConfig.updateNextCursor(cursorRange.getEndCursor());
        memoryRepository.save(recommendBoardConfig);
    }

    private List<RecommendationSimilarBoard> loadRecommendationCsvFileWithinRange(
        RecommendCursorRange cursorRange) {
        List<List<String>> recommendationData = fileStorageService.readRecommendationCsvFile(
            cursorRange.getStartCursor(), cursorRange.getEndCursor());

        Map<String, Integer> header = CsvFileUtil.getHeader(recommendationData);
        List<List<String>> contents = CsvFileUtil.setContents(recommendationData);
        List<RecommendBoardCsvDto> recommendBoardCsvDtoEntities = recommendBoardMapper.mapToCsvDto(header, contents);
        return recommendBoardMapper.mapToEntity(recommendBoardCsvDtoEntities);
    }
}
