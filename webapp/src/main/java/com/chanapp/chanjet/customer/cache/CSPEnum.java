package com.chanapp.chanjet.customer.cache;

import java.util.ArrayList;
import java.util.List;

public class CSPEnum {

    private String enumName;

    private String label;

    private List<CSPEnumValue> enumValues = new ArrayList<CSPEnumValue>();

    public CSPEnum() {
    }

    public CSPEnum(String enumName) {
        this.enumName = enumName;
    }

    public void addEnumValue(CSPEnumValue enumValue) {
        this.enumValues.add(enumValue);
    }

    public String getEnumName() {
        return enumName;
    }

    public void setEnumName(String enumName) {
        this.enumName = enumName;
    }

    public List<CSPEnumValue> getEnumValues() {
        return enumValues;
    }

    public CSPEnumValue getEnumValue(String enumValue) {
        for (CSPEnumValue cspEnumValue : enumValues) {
            if (cspEnumValue.getEnumValue().equals(enumValue)) {
                return cspEnumValue;
            }
        }
        return null;
    }

    public void setEnumValues(List<CSPEnumValue> enumValues) {
        this.enumValues = enumValues;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
