package com.chanapp.chanjet.customer.service.exporttask;

import java.util.ArrayList;
import java.util.List;

public class AttendanceExportData {

    private String email;
    private String mobile;
    private String headPicture;
    private String name;
    private String parentId;
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private String userRole;
    private String userLevel;
    private String actualTimes;// 实际出勤次数
    private String lateTimes;// 迟到次数
    private String leaveEarlyTimes;// 早退次数
    private List<AttendanceExportDayData> daysData;

    public String getActualTimes() {
        return actualTimes;
    }

    public void setActualTimes(String actualTimes) {
        this.actualTimes = actualTimes;
    }

    public String getLateTimes() {
        return lateTimes;
    }

    public void setLateTimes(String lateTimes) {
        this.lateTimes = lateTimes;
    }

    public String getLeaveEarlyTimes() {
        return leaveEarlyTimes;
    }

    public void setLeaveEarlyTimes(String leaveEarlyTimes) {
        this.leaveEarlyTimes = leaveEarlyTimes;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getHeadPicture() {
        return headPicture;
    }

    public void setHeadPicture(String headPicture) {
        this.headPicture = headPicture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(String userLevel) {
        this.userLevel = userLevel;
    }

    public List<AttendanceExportDayData> getDaysData() {
        if (daysData == null) {
            daysData = new ArrayList<AttendanceExportDayData>();
        }
        return daysData;
    }

    public void setDaysData(List<AttendanceExportDayData> daysData) {
        this.daysData = daysData;
    }

}
