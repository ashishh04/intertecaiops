package com.juviai.user.organisation.importer;

import com.juviai.user.organisation.domain.B2BUnitType;
import com.juviai.user.organisation.web.dto.AddressDTO;
import com.juviai.user.organisation.web.dto.OnboardRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
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
public class ExcelB2BUnitImportParser {

    public static final String SHEET_B2B_UNITS = "B2BUnits";

    public List<B2BUnitImportRow> parse(InputStream inputStream, List<ImportRowError> errors) {
        List<B2BUnitImportRow> rows = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(inputStream)) {
            Sheet sheet = wb.getSheet(SHEET_B2B_UNITS);
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

                String name = cellStr(row, 0);
                if (isBlank(name)) {
                    continue;
                }

                try {
                    B2BUnitImportRow r = new B2BUnitImportRow();
                    r.setRowNumber(i + 1);

                    r.setName(requireStr(row, 0, "name", i + 1));
                    r.setType(requireType(row, 1, i + 1));
                    r.setContactEmail(cellStr(row, 2));
                    r.setContactPhone(cellStr(row, 3));
                    r.setWebsite(cellStr(row, 4));
                    r.setLogo(cellStr(row, 5));

                    r.setAddressLine1(requireStr(row, 6, "addressLine1", i + 1));
                    r.setAddressLine2(cellStr(row, 7));
                    r.setCityCode(requireStr(row, 8, "cityCode", i + 1));
                    r.setStateCode(requireStr(row, 9, "stateCode", i + 1));
                    r.setCountry(requireStr(row, 10, "country", i + 1));
                    r.setPostalCode(requireStr(row, 11, "postalCode", i + 1));
                    r.setAddressFullText(cellStr(row, 12));

                    rows.add(r);
                } catch (ImportParseException e) {
                    errors.add(new ImportRowError(i + 1, name, e.getMessage()));
                } catch (Exception e) {
                    errors.add(new ImportRowError(i + 1, name, "Unexpected error: " + e.getMessage()));
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
                try {
                    yield cell.getStringCellValue().trim();
                } catch (Exception ex) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
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

    private B2BUnitType requireType(Row row, int col, int rowNum) {
        String v = cellStr(row, col);
        if (isBlank(v)) {
            throw new ImportParseException("Column 'type' is required (row " + rowNum + ")");
        }
        try {
            return B2BUnitType.valueOf(v.trim().toUpperCase());
        } catch (Exception e) {
            throw new ImportParseException("Invalid type '" + v + "' (row " + rowNum + ")");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public static class B2BUnitImportRow {
        private int rowNumber;
        private String name;
        private B2BUnitType type;
        private String contactEmail;
        private String contactPhone;
        private String website;
        private String logo;
        private String addressLine1;
        private String addressLine2;
        private String cityCode;
        private String stateCode;
        private String country;
        private String postalCode;
        private String addressFullText;

        public int getRowNumber() { return rowNumber; }
        public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public B2BUnitType getType() { return type; }
        public void setType(B2BUnitType type) { this.type = type; }
        public String getContactEmail() { return contactEmail; }
        public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
        public String getContactPhone() { return contactPhone; }
        public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
        public String getWebsite() { return website; }
        public void setWebsite(String website) { this.website = website; }
        public String getLogo() { return logo; }
        public void setLogo(String logo) { this.logo = logo; }
        public String getAddressLine1() { return addressLine1; }
        public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
        public String getAddressLine2() { return addressLine2; }
        public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }
        public String getCityCode() { return cityCode; }
        public void setCityCode(String cityCode) { this.cityCode = cityCode; }
        public String getStateCode() { return stateCode; }
        public void setStateCode(String stateCode) { this.stateCode = stateCode; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public String getAddressFullText() { return addressFullText; }
        public void setAddressFullText(String addressFullText) { this.addressFullText = addressFullText; }

        public OnboardRequest toOnboardRequest() {
            OnboardRequest req = new OnboardRequest();
            req.setName(name);
            req.setType(type);
            req.setContactEmail(contactEmail);
            req.setContactPhone(contactPhone);
            req.setWebsite(website);
            req.setLogo(logo);
            req.setAdminOnboardRequest(true);

            AddressDTO addr = new AddressDTO();
            addr.setLine1(addressLine1);
            addr.setLine2(addressLine2);
            addr.setCity(cityCode);
            addr.setState(stateCode);
            addr.setCountry(country);
            addr.setPostalCode(postalCode);
            addr.setFullText(addressFullText);
            req.setAddress(addr);
            return req;
        }
    }

    public static class ImportRowError {
        private final int rowNumber;
        private final String name;
        private final String message;

        public ImportRowError(int rowNumber, String name, String message) {
            this.rowNumber = rowNumber;
            this.name = name;
            this.message = message;
        }

        public int getRowNumber() { return rowNumber; }
        public String getName() { return name; }
        public String getMessage() { return message; }
    }

    static class ImportParseException extends RuntimeException {
        ImportParseException(String msg) { super(msg); }
    }
}
