package com.bbangle.bbangle.common.domain.csv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class CsvPipeLineImpl<T, U> implements CsvPipLine<T, U> {
    private static final int HEADER_INDEX = 0;

    private Map<String, Integer> headerOrders;
    private List<List<String>> contents;

    public CsvPipeLineImpl(List<List<String>> csvData) {
        setHeader(csvData);
        setContents(csvData);
    }

    @Override
    public List<T> mapToCsvEntity(Function<List<String>, T> mapFunction) {
        return contents.stream().map(mapFunction::apply).toList();
    }

    @Override
    public List<U> mapToClass(List<T> csvEntities, Function<T, U> mapFunction) {
        return csvEntities.stream().map(mapFunction::apply).toList();
    }

    @Override
    public List<List<Object>> mapToList(List<List<Object>> contents) {
        List<Object> headers = headerOrders.entrySet().stream()
            .map(entry -> (Object) entry.getKey())
            .toList();

        return Stream.concat(Stream.of(headers),
                contents.stream().map(row -> row.stream()
                    .toList()))
            .toList();
    }

    public Integer getIndex(String columnName) {
        return headerOrders.get(columnName);
    }

    private void setHeader(List<List<String>> csvData) {
        if (csvData.isEmpty() || csvData.get(HEADER_INDEX).isEmpty()) {
            throw new IllegalArgumentException("CSV 데이터에 헤더가 없습니다.");
        }

        headerOrders = new HashMap<>();

        List<String> headers = csvData.get(HEADER_INDEX);
        for (int i = 0; headers.size() > i; i++) {
            headerOrders.put(headers.get(i), i);
        }
    }

    private void setContents(List<List<String>> csvData) {
        csvData.remove(HEADER_INDEX);
        contents = csvData;
    }
}
