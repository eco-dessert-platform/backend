package com.bbangle.bbangle.common.service.csv;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.common.service.CsvService;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@AutoConfigureMockMvc
class CsvServiceTest {

    private CsvService csvService = new CsvService();

    @Test
    @DisplayName("CSV 파일 전체를 읽어올 수 있다.")
    void readCsvWithRow() {
        // Arrange
        String csvContent = "header1,header2,header3\nvalue1,value2,value3\nvalue4,value5,value6";
        InputStream inputStream = new ByteArrayInputStream(
            csvContent.getBytes(StandardCharsets.UTF_8));

        // Act
        List<List<String>> records = csvService.readCsvWithRow(inputStream);

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
        List<List<String>> records = csvService.readCsvWithRowRange(inputStream, startRow, endRow);

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
        List<List<String>> records = csvService.readCsvWithRowRange(inputStream, startRow, endRow);

        // Assert
        assertThat(records)
            .isNotNull()
            .hasSize(1) // Only header row should be included
            .containsExactly(List.of("header1", "header2", "header3"));
    }

    @Test
    @DisplayName("리스트 데이터를 CSV 형식의 InputStream으로 변환할 수 있다.")
    void convertListToInputStream() throws IOException {
        // Arrange
        List<List<Object>> data = List.of(
            List.of("header1", "header2", "header3"),
            List.of("value1", "value2", "value3"),
            List.of("value4", "value5", "value6")
        );

        // Act
        InputStream inputStream = csvService.convertListToInputStream(data);

        // Assert
        assertThat(inputStream).isNotNull();

        // Read the InputStream to validate its content
        String resultCsv = new BufferedReader(new InputStreamReader(inputStream))
            .lines()
            .collect(Collectors.joining(System.lineSeparator()));

        String expectedCsv = String.join(System.lineSeparator(),
            "header1,header2,header3",
            "value1,value2,value3",
            "value4,value5,value6"
        );

        assertThat(resultCsv).isEqualTo(expectedCsv);
    }

    @Test
    @DisplayName("빈 리스트 데이터를 CSV 형식의 InputStream으로 변환할 수 있다.")
    void convertListToInputStream_EmptyList() throws IOException {
        // Arrange
        List<List<Object>> data = List.of(); // Empty data

        // Act
        InputStream inputStream = csvService.convertListToInputStream(data);

        // Assert
        assertThat(inputStream).isNotNull();

        // Read the InputStream to validate its content
        String resultCsv = new BufferedReader(new InputStreamReader(inputStream))
            .lines()
            .collect(Collectors.joining(System.lineSeparator()));

        assertThat(resultCsv).isEmpty();
    }
}
