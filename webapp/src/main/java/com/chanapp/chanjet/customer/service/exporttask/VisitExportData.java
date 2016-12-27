package com.chanapp.chanjet.customer.service.exporttask;

import java.io.Serializable;
import java.util.Date;

public class VisitExportData implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String userName;
    private Date checkinTime;
    private String checkinTimeStr;
    private Long customerId;
    private String customerName;
    private Integer ccount;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getCheckinTime() {
        return checkinTime;
    }

    public void setCheckinTime(Date checkinTime) {
        this.checkinTime = checkinTime;
    }

    public String getCheckinTimeStr() {
        return checkinTimeStr;
    }

    public void setCheckinTimeStr(String checkinTimeStr) {
        this.checkinTimeStr = checkinTimeStr;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Integer getCcount() {
        return ccount;
    }

    public void setCcount(Integer ccount) {
        this.ccount = ccount;
    }

}
