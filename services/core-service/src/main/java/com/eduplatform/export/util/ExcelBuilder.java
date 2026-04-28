// FILE 25: ExcelBuilder.java
package com.eduplatform.export.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExcelBuilder {
    private String currentSheet = "Sheet1";
    private List<String[]> rows = new ArrayList<>();

    /**
     * CREATE NEW SHEET
     */
    public void createSheet(String sheetName) {
        currentSheet = sheetName;
        rows.clear();
    }

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
     * BUILD EXCEL BYTES
     */
    public byte[] build() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<Workbook>\n");
        xml.append("<Worksheet Name=\"").append(currentSheet).append("\">\n");

        for (String[] row : rows) {
            xml.append("<Row>\n");
            for (String cell : row) {
                xml.append("<Cell>").append(escapeXml(cell)).append("</Cell>\n");
            }
            xml.append("</Row>\n");
        }

        xml.append("</Worksheet>\n");
        xml.append("</Workbook>\n");

        return xml.toString().getBytes();
    }

    /**
     * ESCAPE XML CHARACTERS
     */
    private String escapeXml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}