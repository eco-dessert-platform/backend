package com.bbangle.bbangle.board.domain;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CsvFile {
    String fileName;
    String header;
    StringBuilder body = new StringBuilder();

    public CsvFile(String fileName, String header) {
        this.fileName = fileName;
        this.header = header;
    }

    public CsvFile appendToBody(String text) {
        body.append("\"")
            .append(text)
            .append("\"");
        appendSeparator();
        return this;
    }

    public CsvFile appendToBody(Object text) {
        body.append(text);
        appendSeparator();
        return this;
    }

    public void endAppendBody() {
        deleteEndSeparator();
        body.append("\n");
    }

    private void appendSeparator() {
        body.append(",");
    }

    private void deleteEndSeparator() {
        int length = body.length();
        body.delete(length - 1 , length);
    }

    public String getBody() {
        return body.toString();
    }
}
