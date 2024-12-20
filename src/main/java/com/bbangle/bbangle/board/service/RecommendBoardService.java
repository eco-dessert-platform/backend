package com.bbangle.bbangle.board.service;

import static com.bbangle.bbangle.board.domain.RedisKeyEnum.RECOMMENDATION_LEARNING_CONFIG;

import com.bbangle.bbangle.board.domain.RecommendBoardConfig;
import com.bbangle.bbangle.board.domain.RecommendLearningConfig;
import com.bbangle.bbangle.board.domain.RecommendationSimilarBoard;
import com.bbangle.bbangle.board.domain.RedisKeyEnum;
import com.bbangle.bbangle.board.dto.AiLearningProductDto;
import com.bbangle.bbangle.board.dto.AiLearningReviewDto;
import com.bbangle.bbangle.board.dto.AiLearningStoreDto;
import com.bbangle.bbangle.board.repository.FileStorageRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.board.repository.RecommendBoardRepository;
import com.bbangle.bbangle.board.repository.RecommendationLearningRepository;
import com.bbangle.bbangle.board.repository.temp.RecommendationSimilarBoardMemoryRepository;
import com.bbangle.bbangle.board.dto.RecommendBoardCsvDto;
import com.bbangle.bbangle.board.service.component.RecommendBoardFileStorageComponent;
import com.bbangle.bbangle.board.service.component.RecommendBoardMapper;
import com.bbangle.bbangle.board.util.CsvFileUtil;
import com.bbangle.bbangle.board.domain.RecommendCursorRange;
import com.bbangle.bbangle.review.repository.ReviewRepository;
import com.bbangle.bbangle.store.repository.StoreRepository;
import java.io.*;
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
    private final RecommendationSimilarBoardMemoryRepository recommendationSimilarBoardMemoryRepository;
    private final RecommendationLearningRepository recommendationLearningRepository;
    private final RecommendBoardRepository recommendBoardRepository;
    private final RecommendBoardFileStorageComponent fileStorageService;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final FileStorageRepository fileStorageRepository;

    @Transactional
    public boolean saveRecommendBoardEntity() {
        RecommendBoardConfig recommendBoardConfig = recommendationSimilarBoardMemoryRepository.findById(RedisKeyEnum.RECOMMENDATION_CONFIG)
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
            recommendationSimilarBoardMemoryRepository.save(RecommendBoardConfig.create(fileStorageService.readRowCount(), cursorRange.getEndCursor()));
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
        List<RecommendBoardCsvDto> recommendBoardCsvDtoEntities = recommendBoardMapper.mapToCsvDto(header, contents);
        return recommendBoardMapper.mapToEntity(recommendBoardCsvDtoEntities);
    }

    @Transactional(readOnly = true)
    public boolean uploadAiLearningCsv() {
        RecommendLearningConfig config = recommendationLearningRepository.findById(RECOMMENDATION_LEARNING_CONFIG)
            .orElse(new RecommendLearningConfig());

        if (!config.getIsStoreUploadComplete()) {
            List<AiLearningStoreDto> storeDtos = storeRepository.findAiLearningData();

          try {
            File file = createCsvFileFromDtos1(storeDtos, "testData");
            fileStorageService.uploadCsvFile(file, "store/1.csv");
          } catch (IOException e) {
            throw new RuntimeException(e);
          }

          config.updateIsStoreUploadComplete(true);
            recommendationLearningRepository.save(config);
            return SCHEDULING_CONTINUE;
        }

        if (!config.getIsBoardUploadComplete()) {
            List<AiLearningProductDto> productDtos = productRepository.findAiLearningData();

            try {
                File file = createCsvFileFromDtos2(productDtos, "testData");
                fileStorageService.uploadCsvFile(file, "board/1.csv");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            config.updateIsBoardUploadComplete(true);
            recommendationLearningRepository.save(config);
            return SCHEDULING_CONTINUE;
        }


        if (!config.getIsReviewUploadComplete()) {
            List<AiLearningReviewDto> reviewDtos = reviewRepository.findAiLearningData(
                config.getOffSet(),
                config.getLimit());

            try {
                File file = createCsvFileFromDtos3(reviewDtos, "testData");
                fileStorageService.uploadCsvFile(file, String.format("review/%d.csv", config.getCurrentCursor()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            int dtoSize = reviewDtos.size();

            config.updateIsReviewUploadComplete(dtoSize);
            config.incrementCursor();
            recommendationLearningRepository.save(config);

            return config.continueSchedule(dtoSize);
        }

        recommendationLearningRepository.delete(config);
        return SCHEDULING_STOP;
    }

    private File createCsvFileFromDtos1(List<AiLearningStoreDto> storeDtos, String fileName) throws IOException {
        File tempFile = File.createTempFile(fileName, ".csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            // CSV 헤더 작성
            writer.write("product_board_id,store_id,title");
            writer.newLine();

            // 데이터 작성
            for (AiLearningStoreDto dto : storeDtos) {
                writer.write(dto.getProductBoardId() + "," + dto.getStoreId() + "," + dto.getTitle());
                writer.newLine();
            }
        }

        return tempFile;
    }

    private File createCsvFileFromDtos2(List<AiLearningProductDto> storeDtos, String fileName) throws IOException {
        File tempFile = File.createTempFile(fileName, ".csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            // CSV 헤더 작성
            writer.write("product_board_id,product_id,title");
            writer.newLine();

            // 데이터 작성
            for (AiLearningProductDto dto : storeDtos) {
                writer.write(dto.getProductBoardId() + "," + dto.getProductId() + "," + dto.getTitle());
                writer.newLine();
            }
        }

        return tempFile;
    }

    private File createCsvFileFromDtos3(List<AiLearningReviewDto> storeDtos, String fileName) throws IOException {
        File tempFile = File.createTempFile(fileName, ".csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            // CSV 헤더 작성
            writer.write("product_board_id, title");
            writer.newLine();

            // 데이터 작성
            for (AiLearningReviewDto dto : storeDtos) {
                writer.write(dto.getProductBoardId() + "," + dto.getContent());
                writer.newLine();
            }
        }

        return tempFile;
    }
}
