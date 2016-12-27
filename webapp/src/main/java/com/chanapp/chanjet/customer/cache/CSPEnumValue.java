package com.chanapp.chanjet.customer.cache;

public class CSPEnumValue {

    private String enumValue;

    private String enumLabel;

    private Boolean isActive;

    private String delflag;
    
    private Integer index;

    public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public CSPEnumValue() {
    }

    public CSPEnumValue(String enumValue, String enumLabel, Boolean isActive) {
        this.enumValue = enumValue;
        this.enumLabel = enumLabel;
        this.isActive = isActive;
    }

    public String getEnumValue() {
        return enumValue;
    }

    public void setEnumValue(String enumValue) {
        this.enumValue = enumValue;
    }

    public String getEnumLabel() {
        return enumLabel;
    }

    public void setEnumLabel(String enumLabel) {
        this.enumLabel = enumLabel;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getDelflag() {
        return delflag;
    }

    public void setDelflag(String delflag) {
        this.delflag = delflag;
    }

}
