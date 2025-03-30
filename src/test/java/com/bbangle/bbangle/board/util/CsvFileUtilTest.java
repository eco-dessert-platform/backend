package com.bbangle.bbangle.board.util;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.bbangle.bbangle.util.CsvUtil;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@AutoConfigureMockMvc
class CsvFileUtilTest {

    @Test
    @DisplayName("CSV 파일 전체를 읽어올 수 있다.")
    void readCsvWithRow() {
        // Arrange
        String csvContent = "header1,header2,header3\nvalue1,value2,value3\nvalue4,value5,value6";
        InputStream inputStream = new ByteArrayInputStream(
            csvContent.getBytes(StandardCharsets.UTF_8));

        // Act
        List<List<String>> records = CsvUtil.readCsvWithRow(inputStream);

        // Assert
        assertThat(records)
            .isNotNull()
            .hasSize(3) // Including header row
            .containsExactly(
                List.of("header1", "header2", "header3"),
                List.of("value1", "value2", "value3"),
                List.of("value4", "value5", "value6")
            );
    }

    @Test
    @DisplayName("CSV 파일에서 지정된 행 범위를 읽어올 수 있다.")
    void readCsvWithRowRange() {
        // Arrange
        String csvContent = "header1,header2,header3\nvalue1,value2,value3\nvalue4,value5,value6\nvalue7,value8,value9";
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        int startRow = 1;
        int endRow = 2;

        // Act
        List<List<String>> records = CsvUtil.readCsvWithRowRange(inputStream, startRow, endRow);

        // Assert
        assertThat(records)
            .isNotNull()
            .hasSize(3) // Includes header row
            .containsExactly(
                List.of("header1", "header2", "header3"),
                List.of("value1", "value2", "value3"),
                List.of("value4", "value5", "value6")
            );
    }

    @Test
    @DisplayName("CSV 파일에서 잘못된 범위를 지정했을 때 헤더만 가져오게 된다.")
    void readCsvWithRowRange_InvalidRange() {
        // Arrange
        String csvContent = "header1,header2,header3\nvalue1,value2,value3\nvalue4,value5,value6";
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        int startRow = 5; // Invalid range, out of bounds
        int endRow = 10;

        // Act
        List<List<String>> records = CsvUtil.readCsvWithRowRange(inputStream, startRow, endRow);

        // Assert
        assertThat(records)
            .isNotNull()
            .hasSize(1) // Only header row should be included
            .containsExactly(List.of("header1", "header2", "header3"));
    }
}
