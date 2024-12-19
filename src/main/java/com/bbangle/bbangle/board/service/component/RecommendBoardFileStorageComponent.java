package com.bbangle.bbangle.board.service.component;

import com.bbangle.bbangle.board.dto.AiLearningProductDto;
import com.bbangle.bbangle.board.dto.AiLearningReviewDto;
import com.bbangle.bbangle.board.dto.AiLearningStoreDto;
import com.bbangle.bbangle.board.repository.FileStorageRepository;
import com.bbangle.bbangle.board.util.CsvFileUtil;
import com.bbangle.bbangle.board.util.CsvUtil;
import com.bbangle.bbangle.exception.BbangleException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
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

    private final String STORE_ID = "store_id";
    private final String BOARD_ID = "product_board_id";
    private final String PRODUCT_ID = "product_id";
    private final String CONTENTS = "content";
    private final String TITLE = "title";

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

    public InputStream convertStoreDtosToInputStream(List<AiLearningStoreDto> dtos) {
        StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(BOARD_ID, STORE_ID, TITLE))) {
            for (AiLearningStoreDto dto : dtos) {
                printer.printRecord(dto.getProductBoardId(), dto.getStoreId(), dto.getTitle());
            }
        } catch (IOException e){
            throw new BbangleException();
        }
        return new ByteArrayInputStream(out.toString().getBytes(StandardCharsets.UTF_8));
    }

    public InputStream convertReviewDtosToInputStream(List<AiLearningReviewDto> dtos) {
        StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(BOARD_ID, CONTENTS))) {
            for (AiLearningReviewDto dto : dtos) {
                printer.printRecord(dto.getProductBoardId(), dto.getContent());
            }
        } catch (IOException e){
            throw new BbangleException();
        }
        return new ByteArrayInputStream(out.toString().getBytes(StandardCharsets.UTF_8));
    }

    public InputStream convertProductDtosToInputStream(List<AiLearningProductDto> dtos) {
        StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(BOARD_ID, PRODUCT_ID, TITLE))) {
            for (AiLearningProductDto dto : dtos) {
                printer.printRecord(dto.getProductBoardId(), dto.getProductId(), dto.getTitle());
            }
        } catch (IOException e){
            throw new BbangleException();
        }
        return new ByteArrayInputStream(out.toString().getBytes(StandardCharsets.UTF_8));
    }

    public void uploadCsvFile(InputStream file) {
        long fileByteSize;
        try {
            fileByteSize = CsvFileUtil.getInputStreamSize(file);
        } catch (IOException e){
            throw new BbangleException();
        }

        fileStorageRepository.uploadFile(
            file,
            recommendSourceDataFileUrl,
            "text/csv",
            fileByteSize);
    }
}
