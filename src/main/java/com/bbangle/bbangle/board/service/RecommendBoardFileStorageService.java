package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.common.service.CsvService;
import com.bbangle.bbangle.common.service.FileStorageServiceImpl;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendBoardFileStorageService {

    private static final String RECOMMEND_BOARD_CONFIG_FILE_URL = "resource/ai/recommendaion/RecommendedAIConfig.csv";
    private static final String RECOMMEND_BOARD_DATA_FILE_URL = "resource/ai/recommendaion/RecommendedAIData.csv";
    private static final String CSV_CONTENT_TYPE = "text/csv";
    private static final Integer CONTENTS_LENGTH = 500;

    private final FileStorageServiceImpl fileStorageServiceImpl;
    private final CsvService csvService;

    public List<List<String>> readRecommendationConfigCsvFile() {
        InputStream file = fileStorageServiceImpl.getFile(RECOMMEND_BOARD_CONFIG_FILE_URL);

        return csvService.readCsvWithRow(file);
    }

    public List<List<String>> readRecommendationCsvFile(int startRow, int endRow) {
        InputStream file = fileStorageServiceImpl.getFile(RECOMMEND_BOARD_DATA_FILE_URL);

        return csvService.readCsvWithRowRange(file, startRow, endRow);
    }

    public void uploadRecommendationConfigCsvFile(List<List<Object>> csvData) {
        InputStream csvFile = csvService.convertListToInputStream(csvData);
        fileStorageServiceImpl.uploadStreamFile(
            csvFile,
            RECOMMEND_BOARD_CONFIG_FILE_URL,
            CSV_CONTENT_TYPE,
            CONTENTS_LENGTH
        );
    }
}
