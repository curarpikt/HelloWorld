package com.chanapp.chanjet.customer.service.exporttask;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class ExportDataValue {
    private List<List<ExcelColunmValue>> sheetFieldNames;
    private List<List<Map<String, Object>>> sheetDatas;
    private Timestamp startDate;
    private Timestamp endDate;

    public List<List<ExcelColunmValue>> getSheetFieldNames() {
        return sheetFieldNames;
    }

    public void setSheetFieldNames(List<List<ExcelColunmValue>> sheetFieldNames) {
        this.sheetFieldNames = sheetFieldNames;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public List<List<Map<String, Object>>> getSheetDatas() {
        return sheetDatas;
    }

    public void setSheetDatas(List<List<Map<String, Object>>> sheetDatas) {
        this.sheetDatas = sheetDatas;
    }

}
