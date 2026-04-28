// FILE 24: PdfGenerator.java
package com.eduplatform.export.util;

import java.util.ArrayList;
import java.util.List;

public class PdfGenerator {
    private List<String> content = new ArrayList<>();

    /**
     * ADD TITLE
     */
    public void addTitle(String title) {
        content.add("TITLE:" + title);
    }

    /**
     * ADD TEXT
     */
    public void addText(String text) {
        content.add("TEXT:" + text);
    }

    /**
     * ADD TABLE
     */
    public void addTable(String[] headers, List<String[]> rows) {
        content.add("TABLE_START");
        content.add("HEADERS:" + String.join(",", headers));
        for (String[] row : rows) {
            content.add("ROW:" + String.join(",", row));
        }
        content.add("TABLE_END");
    }

    /**
     * GENERATE PDF BYTES
     */
    public byte[] generate() {
        String pdfContent = String.join("\n", content);
        return pdfContent.getBytes();
    }
}