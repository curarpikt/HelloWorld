package com.chanapp.chanjet.customer.service.report;

import com.chanapp.chanjet.customer.util.PinyinUtil;
import com.chanjet.csp.cmr.api.metadata.base.field.generated.FieldTypeEnum;

public class FieldRestObject implements Comparable<FieldRestObject> {

    private String id;

    private String name;

    private FieldTypeEnum fieldType;

    private String label;

    private boolean systemField;

    private String enumName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FieldTypeEnum getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldTypeEnum fieldType) {
        this.fieldType = fieldType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isSystemField() {
        return systemField;
    }

    public void setSystemField(boolean systemField) {
        this.systemField = systemField;
    }

    public int compareTo(FieldRestObject o) {
        String chars = PinyinUtil.hanziToPinyinSimple(this.label, false);
        String compareChars = PinyinUtil.hanziToPinyinSimple(o.getLabel(), false);
        return chars.compareTo(compareChars);
    }

    public String getEnumName() {
        return enumName;
    }

    public void setEnumName(String enumName) {
        this.enumName = enumName;
    }

}
