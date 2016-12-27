package com.chanapp.chanjet.customer.service.contactremindset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContactRemindSetsValue {
    private String enumName;
    private Integer errorCode;
    private String errorMessage;
    private boolean isChange;
    private Long modifyTime;
    private List<Map<String, Object>> values;

    public String getEnumName() {
        return enumName;
    }

    public void setEnumName(String enumName) {
        this.enumName = enumName;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isChange() {
        return isChange;
    }

    public void setChange(boolean isChange) {
        this.isChange = isChange;
    }

    public Long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public List<Map<String, Object>> getValues() {
        if (values == null) {
            values = new ArrayList<Map<String, Object>>();
        }
        return values;
    }

    public void setValues(List<Map<String, Object>> values) {
        this.values = values;
    }

}
