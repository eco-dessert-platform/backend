package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.domain.*;
import com.bbangle.bbangle.board.dto.AiLearningProductDto;
import com.bbangle.bbangle.board.dto.AiLearningReviewDto;
import com.bbangle.bbangle.board.dto.AiLearningStoreDto;
import com.bbangle.bbangle.board.dto.RecommendBoardCsvDto;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.board.repository.RecommendAiBoardRepository;
import com.bbangle.bbangle.board.repository.RecommendationLearningRepository;
import com.bbangle.bbangle.board.repository.RecommendationSimilarBoardMemoryRepository;
import com.bbangle.bbangle.board.service.component.RecommendBoardFileStorageComponent;
import com.bbangle.bbangle.board.service.component.RecommendBoardMapper;
import com.bbangle.bbangle.board.util.CsvFileUtil;
import com.bbangle.bbangle.review.repository.ReviewRepository;
import com.bbangle.bbangle.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.bbangle.bbangle.board.domain.RedisKeyEnum.RECOMMENDATION_LEARNING_CONFIG;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendAiBoardService {

    private static final boolean SCHEDULING_CONTINUE = true;
    private static final boolean SCHEDULING_STOP = false;
    private static final int MAX_PRODUCT_COUNT_NULL = -1;
    private static final String STORE_CSV_FILE_NAME = "store/1.csv";
    private static final String BOARD_CSV_FILE_NAME = "board/1.csv";
    private static final String REVIEW_CSV_FILE_NAME = "review/%d.csv";
    private static final String STORE_CSV_HEADER = "store_id,product_board_id,title";
    private static final String BOARD_CSV_HEADER = "product_board_id,product_id,title";
    private static final String REVIEW_CSV_HEADER = "product_board_id,title";
    private final RecommendBoardMapper recommendBoardMapper;
    private final RecommendationSimilarBoardMemoryRepository recommendationSimilarBoardMemoryRepository;
    private final RecommendationLearningRepository recommendationLearningRepository;
    private final RecommendAiBoardRepository recommendAiBoardRepository;
    private final RecommendBoardFileStorageComponent fileStorageService;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public boolean saveRecommendBoardEntity() {
        RecommendBoardConfig recommendBoardConfig = recommendationSimilarBoardMemoryRepository.findById(
                RedisKeyEnum.RECOMMENDATION_CONFIG)
            .orElse(RecommendBoardConfig.empty());

        RecommendCursorRange cursorRange = RecommendCursorRange.builder()
            .maxProductCount(recommendBoardConfig.getMaxProductCount())
            .nextCursor(recommendBoardConfig.getNextCursor())
            .build();

        if (!recommendBoardConfig.getMaxProductCount().equals(MAX_PRODUCT_COUNT_NULL)
            && cursorRange.isEnd()) {
            return SCHEDULING_STOP;
        }

        List<RecommendationSimilarBoard> recommendationSimilarBoards = loadRecommendationCsvFileWithinRange(
            cursorRange);

        recommendAiBoardRepository.saveAll(recommendationSimilarBoards);

        initializeOrUpdateBoardConfig(recommendBoardConfig, cursorRange);

        return SCHEDULING_CONTINUE;
    }

    private void initializeOrUpdateBoardConfig(RecommendBoardConfig recommendBoardConfig,
        RecommendCursorRange cursorRange) {
        if (recommendBoardConfig.getMaxProductCount().equals(MAX_PRODUCT_COUNT_NULL)) {
            recommendationSimilarBoardMemoryRepository.save(
                RecommendBoardConfig.create(fileStorageService.readRowCount(),
                    cursorRange.getEndCursor()));
            return;
        }

        recommendBoardConfig.updateNextCursor(cursorRange.getEndCursor());
        recommendationSimilarBoardMemoryRepository.save(recommendBoardConfig);
    }

    private List<RecommendationSimilarBoard> loadRecommendationCsvFileWithinRange(
        RecommendCursorRange cursorRange) {
        List<List<String>> recommendationData = fileStorageService.readRecommendationCsvFile(
            cursorRange.getStartCursor(), cursorRange.getEndCursor());

        Map<String, Integer> header = CsvFileUtil.getHeader(recommendationData);
        List<List<String>> contents = CsvFileUtil.setContents(recommendationData);
        List<RecommendBoardCsvDto> recommendBoardCsvDtoEntities = recommendBoardMapper.mapToCsvDto(
            header, contents);
        return recommendBoardMapper.mapToEntity(recommendBoardCsvDtoEntities);
    }

    @Transactional(readOnly = true)
    public boolean uploadAiLearningCsv() {
        RecommendLearningConfig config = recommendationLearningRepository.findById(RECOMMENDATION_LEARNING_CONFIG)
            .orElse(new RecommendLearningConfig());

        if (config.isNotStoreUploadComplete()) {
            uploadStoreCsv();
            saveStoreConfig(config);
            return SCHEDULING_CONTINUE;
        } else if (config.isNotBoardUploadComplete()) {
            uploadBoardCsv();
            saveBoardConfig(config);
            return SCHEDULING_CONTINUE;
        } else if (config.isNotReviewUploadComplete()) {
            List<AiLearningReviewDto> reviewDtos = reviewRepository.findAiLearningData(
                config.getOffSet(),
                config.getLimit());

            int dtoSize = reviewDtos.size();

            uploadReviewCsv(config, reviewDtos);
            saveReviewConfig(config, dtoSize);
            return SCHEDULING_CONTINUE;
        }

        recommendationLearningRepository.delete(config);
        return SCHEDULING_STOP;
    }
    private void uploadStoreCsv() {
        List<AiLearningStoreDto> storeDtos = storeRepository.findAiLearningData();

        CsvFile csvFile = new CsvFile(STORE_CSV_FILE_NAME, STORE_CSV_HEADER);
        storeDtos.forEach(dto -> csvFile.appendToBody(dto.getStoreId())
            .appendToBody(dto.getProductBoardId())
            .appendToBody(dto.getTitle())
            .endAppendBody());

        fileStorageService.uploadCsvFile(csvFile);
    }

    private void saveStoreConfig(RecommendLearningConfig config) {
        config.updateIsStoreUploadComplete();
        recommendationLearningRepository.save(config);
    }

    private void uploadBoardCsv() {
        List<AiLearningProductDto> productDtos = productRepository.findAiLearningData();

        CsvFile csvFile = new CsvFile(BOARD_CSV_FILE_NAME, BOARD_CSV_HEADER);
        productDtos.forEach(dto -> csvFile.appendToBody(dto.getProductBoardId())
            .appendToBody(dto.getProductId())
            .appendToBody(dto.getTitle())
            .endAppendBody());

        fileStorageService.uploadCsvFile(csvFile);
    }

    private void saveBoardConfig(RecommendLearningConfig config) {
        config.updateIsBoardUploadComplete();
        recommendationLearningRepository.save(config);
    }

    private void uploadReviewCsv(RecommendLearningConfig config, List<AiLearningReviewDto> reviewDtos) {
        String fileName = String.format(REVIEW_CSV_FILE_NAME, config.getCurrentCursor());
        CsvFile csvFile = new CsvFile(fileName, REVIEW_CSV_HEADER);
        reviewDtos.forEach(dto -> csvFile.appendToBody(dto.getProductBoardId())
            .appendToBody(dto.getContent())
            .endAppendBody());

        fileStorageService.uploadCsvFile(csvFile);
    }

    private void saveReviewConfig(RecommendLearningConfig config, Integer dtoSize) {
        config.updateIsReviewUploadComplete(dtoSize);
        config.incrementCursor();
        recommendationLearningRepository.save(config);
    }
}
