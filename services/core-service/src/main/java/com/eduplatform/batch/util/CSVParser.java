// FILE 31: CSVParser.java
package com.eduplatform.batch.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class CSVParser {

    /**
     * PARSE CSV FILE
     */
    public static List<Map<String, String>> parseCSV(InputStream input) throws Exception {
        List<Map<String, String>> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String line;
            String[] headers = null;
            int lineNum = 0;

            while ((line = reader.readLine()) != null) {
                if (lineNum == 0) {
                    headers = line.split(",");
                } else {
                    String[] values = line.split(",");
                    Map<String, String> record = new HashMap<>();

                    for (int i = 0; i < headers.length && i < values.length; i++) {
                        record.put(headers[i].trim(), values[i].trim());
                    }

                    records.add(record);
                }
                lineNum++;
            }
        }

        return records;
    }
}