package com.bbangle.bbangle.board.service.component;

import com.bbangle.bbangle.board.domain.CsvFile;
import com.bbangle.bbangle.board.dto.AiLearningProductDto;
import com.bbangle.bbangle.board.dto.AiLearningReviewDto;
import com.bbangle.bbangle.board.dto.AiLearningStoreDto;
import com.bbangle.bbangle.board.repository.FileStorageRepository;
import com.bbangle.bbangle.board.util.CsvFileUtil;
import com.bbangle.bbangle.board.util.CsvUtil;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendBoardFileStorageComponent {

    @Value("${cloud.aws.file-path.recommend-board-data-file-url}")
    private String recommendDataFileUrl;

    @Value("${cloud.aws.file-path.recommend-board-resource-data-url}")
    private String recommendSourceDataFileUrl;

    private final FileStorageRepository fileStorageRepository;

    public List<List<String>> readRecommendationCsvFile(int startRow, int endRow) {
        InputStream file = fileStorageRepository.getFile(recommendDataFileUrl);

        return CsvUtil.readCsvWithRowRange(file, startRow, endRow);
    }

    public int readRowCount() {
        InputStream file = fileStorageRepository.getFile(recommendDataFileUrl);

        return CsvUtil.getCsvRowCount(file);
    }

    public void uploadCsvFile(CsvFile csvFile) {
        File file;
        try {
            file = CsvUtil.createCsvFile(csvFile.getFileName(), csvFile.getHeader(), csvFile.getBody());
        } catch (IOException e) {
            throw new BbangleException(BbangleErrorCode.CSV_NOT_CONVERT_ERROR, e);
        }

        String fileUrl = recommendSourceDataFileUrl + csvFile.getFileName();
        fileStorageRepository.uploadFile(fileUrl, file);
    }
}