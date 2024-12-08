package com.bbangle.bbangle.board.util;

import static com.bbangle.bbangle.exception.BbangleErrorCode.CSV_NOT_READ_ERROR;

import com.bbangle.bbangle.exception.BbangleException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CsvUtil {

    private static final int HEADER_ROW_COUNT = 1;

    public static List<List<String>> readCsvWithRow(InputStream inputStream) {
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            Iterable<CSVRecord> csvRecords = CSVFormat.DEFAULT.parse(reader);

            for (CSVRecord csvRecord : csvRecords) {
                List<String> row = new ArrayList<>();
                csvRecord.forEach(row::add);
                records.add(row);
            }
        } catch (Exception e) {
            throw new BbangleException(CSV_NOT_READ_ERROR);
        } finally {
            closeInputStream(inputStream);
        }
        return records;
    }

    public static List<List<String>> readCsvWithRowRange(InputStream inputStream, int startRow,
        int endRow) {
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            Iterable<CSVRecord> csvRecords = CSVFormat.DEFAULT.parse(reader);

            int currentRow = 0;
            for (CSVRecord csvRecord : csvRecords) {
                if (currentRow == 0 || (currentRow >= startRow && currentRow <= endRow)) {
                    List<String> row = new ArrayList<>();
                    csvRecord.forEach(row::add);
                    records.add(row);
                }
                if (currentRow > endRow) {
                    break;
                }
                currentRow++;
            }

        } catch (Exception e) {
            throw new BbangleException(CSV_NOT_READ_ERROR);
        } finally {
            closeInputStream(inputStream);
        }

        return records;
    }

    public static int getCsvRowCount(InputStream inputStream) throws IOException {
        int rowCount = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            Iterable<CSVRecord> csvRecords = CSVFormat.DEFAULT.parse(reader);

            for (CSVRecord ignored : csvRecords) {
                rowCount++;
            }

            inputStream.close();
        } catch (Exception e) {
            throw new BbangleException(CSV_NOT_READ_ERROR);
        }
        finally {
            closeInputStream(inputStream);
        }

        return rowCount - HEADER_ROW_COUNT;
    }

    private static void closeInputStream(InputStream inputStream) {
        try {
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
