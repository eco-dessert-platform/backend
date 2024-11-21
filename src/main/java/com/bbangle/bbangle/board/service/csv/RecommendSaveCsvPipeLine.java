package com.bbangle.bbangle.board.service.csv;

public interface RecommendSaveCsvPipeLine<T, U> {
    U mapToEntity(T csvEntity);
}
