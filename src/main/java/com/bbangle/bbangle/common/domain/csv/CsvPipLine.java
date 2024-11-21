package com.bbangle.bbangle.common.domain.csv;

import java.util.List;
import java.util.function.Function;

public interface CsvPipLine<T, U> {
    List<T> mapToCsvEntity(Function<List<String>, T> mapFunction);
    List<U> mapToClass(List<T> recommendBoards, Function<T, U> mapFunction);
    List<List<Object>> mapToList(List<List<Object>> contents);
}
