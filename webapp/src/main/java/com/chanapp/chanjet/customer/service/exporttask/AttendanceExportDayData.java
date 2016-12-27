package com.chanapp.chanjet.customer.service.exporttask;

public class AttendanceExportDayData {
    private String absence;// 0-缺勤 1-未缺勤
    private String onTime;// 上班时间
    private String offTime;// 下班时间
    private String onStatus;// 上班签到状态

    public String getAbsence() {
        return absence;
    }

    public void setAbsence(String absence) {
        this.absence = absence;
    }

    private String offStatus;// 下班签到状态
    private String onAbnormalTime;// 上班迟到分钟数
    private String offAbnormalTime;// 下班早退分钟数

    public String getOnTime() {
        return onTime;
    }

    public void setOnTime(String onTime) {
        this.onTime = onTime;
    }

    public String getOffTime() {
        return offTime;
    }

    public void setOffTime(String offTime) {
        this.offTime = offTime;
    }

    public String getOnStatus() {
        return onStatus;
    }

    public void setOnStatus(String onStatus) {
        this.onStatus = onStatus;
    }

    public String getOffStatus() {
        return offStatus;
    }

    public void setOffStatus(String offStatus) {
        this.offStatus = offStatus;
    }

    public String getOnAbnormalTime() {
        return onAbnormalTime;
    }

    public void setOnAbnormalTime(String onAbnormalTime) {
        this.onAbnormalTime = onAbnormalTime;
    }

    public String getOffAbnormalTime() {
        return offAbnormalTime;
    }

    public void setOffAbnormalTime(String offAbnormalTime) {
        this.offAbnormalTime = offAbnormalTime;
    }

}
