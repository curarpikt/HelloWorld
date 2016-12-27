package com.chanapp.chanjet.customer.service.report;

import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.customer.vo.RowSet;
import com.chanapp.chanjet.web.service.BaseServiceItf;

public interface ReportServiceItf extends BaseServiceItf {
    RowSet getAddWeekCustomerAnalysisData(String params);

    RowSet getCompositionAnalysisData(String params);

    Row attendanceCountCsv(Long groupId, Long userId, String countDate);

    Row checkinVisitCount(Long groupId, Long userId, String countDate, String countType);

    List<Map<String, Object>> checkinVisitDetail(Long userId, Long startDate, Long endDate);

    RowSet customerProgressCount(String countType, Long userId, Long startDate, Long endDate);

    List<FieldRestObject> getAllCustomerEnums();

    Row customerProgress(String countType, Long userId, Long startDate, Long endDate);

    Row workrecordProgress(String countType);

    Row attendanceCount(Long groupId, Long userId, String countDate);
}
