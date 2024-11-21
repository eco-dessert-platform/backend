package com.bbangle.bbangle.board.service.csv;

import static com.bbangle.bbangle.exception.BbangleErrorCode.NOT_VALID_INDEX;

import com.bbangle.bbangle.common.domain.csv.CsvPipeLineImpl;
import com.bbangle.bbangle.exception.BbangleException;
import java.util.ArrayList;
import java.util.List;

public class RecommendBoardConfigPipeLine implements
    RecommendDefaultCsvPipeLine<RecommendBoardConfigCsvEntity>,
    RecommendUploadCsvPipeLine<RecommendBoardConfigCsvEntity> {

    private static final String MAX_PRODUCT_COUNT = "MAX_PRODUCT_COUNT";
    private static final String NEXT_CURSOR = "NEXT_CURSOR";
    private static final int FIRST_ITEM_INDEX = 0;
    private static final int MIN_INDEX = 0;
    private CsvPipeLineImpl<RecommendBoardConfigCsvEntity, Object> csvPipeLine;

    public RecommendBoardConfigPipeLine(List<List<String>> csvData) {
        csvPipeLine = new CsvPipeLineImpl<>(csvData);
    }

    @Override
    public RecommendBoardConfigCsvEntity mapToCsvEntity() {
        return csvPipeLine.mapToCsvEntity(row ->
                RecommendBoardConfigCsvEntity.builder()
                    .maxProductCount(Integer.valueOf(row.get(csvPipeLine.getIndex(MAX_PRODUCT_COUNT))))
                    .nextCursor(Integer.valueOf(row.get(csvPipeLine.getIndex(NEXT_CURSOR))))
                    .build())
            .get(FIRST_ITEM_INDEX);
    }

    @Override
    public List<List<Object>> mapToList(RecommendBoardConfigCsvEntity entity) {
        int maxProductCountIndex = csvPipeLine.getIndex(MAX_PRODUCT_COUNT);
        int nextCursor = csvPipeLine.getIndex(NEXT_CURSOR);

        List<List<Object>> resultList = new ArrayList<>();
        List<Object> firstList = new ArrayList<>();

        addFieldValue(firstList, maxProductCountIndex, entity.getMaxProductCount());
        addFieldValue(firstList, nextCursor, entity.getNextCursor());

        resultList.add(firstList);

        return csvPipeLine.mapToList(resultList);
    }

    private void addFieldValue(List<Object> resultList, int index, Object row) {
        if (index < MIN_INDEX) {
            throw new BbangleException(NOT_VALID_INDEX);
        }

        resultList.add(row);
    }
}
