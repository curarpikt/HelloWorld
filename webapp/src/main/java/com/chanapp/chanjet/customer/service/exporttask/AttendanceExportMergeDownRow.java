package com.chanapp.chanjet.customer.service.exporttask;

import java.util.ArrayList;
import java.util.List;

public class AttendanceExportMergeDownRow {
    private String parentName;// 组名

    public String getParentuserId() {
        return parentuserId;
    }

    public void setParentuserId(String parentuserId) {
        this.parentuserId = parentuserId;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    private String parentuserId;// id
    private String userRole;// 角色
    private List<AttendanceExportData> exportDataList;// 实际的ROW

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public List<AttendanceExportData> getExportDataList() {
        if (exportDataList == null) {
            exportDataList = new ArrayList<AttendanceExportData>();
        }
        return exportDataList;
    }

    public void setExportDataList(List<AttendanceExportData> exportDataList) {
        this.exportDataList = exportDataList;
    }

}
