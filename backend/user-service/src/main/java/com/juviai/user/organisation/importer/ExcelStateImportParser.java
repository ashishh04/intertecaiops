package com.juviai.user.organisation.importer;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ExcelStateImportParser {

    public static final String SHEET_STATES = "States";

    public List<StateImportRow> parse(InputStream inputStream, List<ImportRowError> errors) {
        List<StateImportRow> rows = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(inputStream)) {
            Sheet sheet = wb.getSheet(SHEET_STATES);
            if (sheet == null) {
                sheet = wb.getNumberOfSheets() > 0 ? wb.getSheetAt(0) : null;
            }
            if (sheet == null) {
                errors.add(new ImportRowError(0, null, "No sheet found in workbook"));
                return rows;
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String code = cellStr(row, 1);
                if (isBlank(code)) {
                    continue;
                }

                try {
                    StateImportRow r = new StateImportRow();
                    r.setRowNumber(i + 1);
                    r.setName(requireStr(row, 0, "name", i + 1));
                    r.setCode(requireStr(row, 1, "code", i + 1));
                    r.setActive(optionalBool(row, 2, true));
                    r.setCountryCode(cellStr(row, 3));
                    rows.add(r);
                } catch (ImportParseException e) {
                    errors.add(new ImportRowError(i + 1, code, e.getMessage()));
                } catch (Exception e) {
                    errors.add(new ImportRowError(i + 1, code, "Unexpected error: " + e.getMessage()));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to read workbook", e);
            errors.add(new ImportRowError(0, null, "Failed to read workbook: " + e.getMessage()));
        }

        return rows;
    }

    private String cellStr(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                yield (d == Math.floor(d)) ? String.valueOf((long) d) : String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield cell.getStringCellValue().trim(); }
                catch (Exception ex) { yield String.valueOf(cell.getNumericCellValue()); }
            }
            default -> null;
        };
    }

    private String requireStr(Row row, int col, String fieldName, int rowNum) {
        String v = cellStr(row, col);
        if (isBlank(v)) {
            throw new ImportParseException("Column '" + fieldName + "' is required (row " + rowNum + ")");
        }
        return v;
    }

    private boolean optionalBool(Row row, int col, boolean defaultVal) {
        String v = cellStr(row, col);
        if (isBlank(v)) return defaultVal;
        String x = v.trim().toUpperCase();
        return "TRUE".equals(x) || "YES".equals(x) || "1".equals(x);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public static class StateImportRow {
        private int rowNumber;
        private String name;
        private String code;
        private boolean active = true;
        private String countryCode;

        public int getRowNumber() { return rowNumber; }
        public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    }

    public static class ImportRowError {
        private final int rowNumber;
        private final String code;
        private final String message;

        public ImportRowError(int rowNumber, String code, String message) {
            this.rowNumber = rowNumber;
            this.code = code;
            this.message = message;
        }

        public int getRowNumber() { return rowNumber; }
        public String getCode() { return code; }
        public String getMessage() { return message; }
    }

    static class ImportParseException extends RuntimeException {
        ImportParseException(String msg) { super(msg); }
    }
}
