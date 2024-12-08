package com.bbangle.bbangle.board.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CsvFileUtil {
    private static final int HEADER_INDEX = 0;
    public static Map<String, Integer> getHeader(List<List<String>> csvData) {
        if (csvData.isEmpty() || csvData.get(HEADER_INDEX).isEmpty()) {
            throw new IllegalArgumentException("CSV 데이터에 헤더가 없습니다.");
        }

        Map<String, Integer> headerOrders = new HashMap<>();

        List<String> headers = csvData.get(HEADER_INDEX);
        for (int i = 0; headers.size() > i; i++) {
            headerOrders.put(headers.get(i), i);
        }

        return headerOrders;
    }

    public static List<List<String>> setContents(List<List<String>> csvData) {
        csvData.remove(HEADER_INDEX);
        return csvData;
    }

}
