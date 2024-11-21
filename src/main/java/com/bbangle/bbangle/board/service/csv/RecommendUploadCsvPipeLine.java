package com.bbangle.bbangle.board.service.csv;

import java.util.List;

public interface RecommendUploadCsvPipeLine<T> {
    List<List<Object>> mapToList(T entity);
}
