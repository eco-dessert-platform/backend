package com.bbangle.bbangle.board.service.component;

import com.bbangle.bbangle.board.repository.FileStorageRepository;
import com.bbangle.bbangle.board.util.CsvUtil;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendBoardFileStorageComponent {

    @Value("${cloud.aws.file-path.recommend-board-data-file-url}")
    private String recommendDataFileUrl;

    private final FileStorageRepository fileStorageRepository;

    public List<List<String>> readRecommendationCsvFile(int startRow, int endRow) {
        InputStream file = fileStorageRepository.getFile(recommendDataFileUrl);

        return CsvUtil.readCsvWithRowRange(file, startRow, endRow);
    }

    public int readRowCount() {
        InputStream file = fileStorageRepository.getFile(recommendDataFileUrl);

        try {
            return CsvUtil.getCsvRowCount(file);
        } catch (IOException e) {
            throw new BbangleException(BbangleErrorCode.INPUT_STREAM_NOT_CLOSE);
        }
    }

}
