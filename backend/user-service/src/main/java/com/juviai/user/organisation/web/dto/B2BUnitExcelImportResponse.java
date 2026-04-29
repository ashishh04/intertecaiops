package com.juviai.user.organisation.web.dto;

import com.juviai.user.organisation.importer.ExcelB2BUnitImportParser;

import java.util.ArrayList;
import java.util.List;

public class B2BUnitExcelImportResponse {

    private int totalRows;
    private int createdCount;
    private int failedCount;
    private List<ExcelB2BUnitImportParser.ImportRowError> errors = new ArrayList<>();

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getCreatedCount() {
        return createdCount;
    }

    public void setCreatedCount(int createdCount) {
        this.createdCount = createdCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public List<ExcelB2BUnitImportParser.ImportRowError> getErrors() {
        return errors;
    }

    public void setErrors(List<ExcelB2BUnitImportParser.ImportRowError> errors) {
        this.errors = errors;
    }
}
