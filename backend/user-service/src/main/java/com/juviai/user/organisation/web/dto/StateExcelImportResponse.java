package com.juviai.user.organisation.web.dto;

import com.juviai.user.organisation.importer.ExcelStateImportParser;

import java.util.ArrayList;
import java.util.List;

public class StateExcelImportResponse {

    private int totalRows;
    private int createdCount;
    private int failedCount;
    private List<ExcelStateImportParser.ImportRowError> errors = new ArrayList<>();

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

    public List<ExcelStateImportParser.ImportRowError> getErrors() {
        return errors;
    }

    public void setErrors(List<ExcelStateImportParser.ImportRowError> errors) {
        this.errors = errors;
    }
}
