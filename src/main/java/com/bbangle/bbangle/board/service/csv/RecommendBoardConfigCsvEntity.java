package com.bbangle.bbangle.board.service.csv;

import com.bbangle.bbangle.common.domain.csv.CsvSelectEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendBoardConfigCsvEntity implements CsvSelectEntity {
    private Integer maxProductCount;
    private Integer nextCursor;

    public void updateNextCursor(int nextCursor) {
        this.nextCursor = nextCursor;
    }
}
