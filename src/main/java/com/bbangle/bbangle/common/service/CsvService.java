package com.bbangle.bbangle.common.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

@Service
public class CsvService {

    public List<List<String>> readCsvWithRow(InputStream inputStream) {
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            Iterable<CSVRecord> csvRecords = CSVFormat.DEFAULT.parse(reader);

            for (CSVRecord csvRecord : csvRecords) {
                List<String> row = new ArrayList<>();
                csvRecord.forEach(row::add);
                records.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return records;
    }

    public List<List<String>> readCsvWithRowRange(InputStream inputStream, int startRow,
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
            e.printStackTrace();
        }
        return records;
    }

    public InputStream convertListToInputStream(List<List<Object>> data) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (CSVPrinter csvPrinter = new CSVPrinter(
            new OutputStreamWriter(byteArrayOutputStream),
            CSVFormat.Builder.create(CSVFormat.DEFAULT)
                .setRecordSeparator(System.lineSeparator())
                .build())) {
            for (List<Object> row : data) {
                csvPrinter.printRecord(row);
            }

            csvPrinter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
}
