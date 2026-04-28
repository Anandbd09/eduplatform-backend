// FILE 23: CsvBuilder.java
package com.eduplatform.export.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CsvBuilder {
    private List<String[]> rows = new ArrayList<>();

    /**
     * ADD HEADER ROW
     */
    public void addHeader(String... headers) {
        rows.add(headers);
    }

    /**
     * ADD DATA ROW
     */
    public void addRow(Map<String, String> data) {
        String[] row = data.values().toArray(new String[0]);
        rows.add(row);
    }

    /**
     * BUILD CSV CONTENT
     */
    public String build() {
        StringBuilder csv = new StringBuilder();

        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                if (i > 0) csv.append(",");
                csv.append("\"").append(escapeCsv(row[i])).append("\"");
            }
            csv.append("\n");
        }

        return csv.toString();
    }

    /**
     * ESCAPE CSV VALUES
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}