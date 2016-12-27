package com.chanapp.chanjet.customer.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerMetaData {

    private Map<String, CspEntityRestObject> entites = new HashMap<String, CspEntityRestObject>();

    private Map<String, CSPEnum> enums = new HashMap<String, CSPEnum>();

    private Long version = VersionInfo.getInstance().getLastModifiedDate().getTime();

    private CustomerLayout layout;

    private Map<String, List<String>> disableFields = new HashMap<String, List<String>>();

    public Map<String, List<String>> getDisableFields() {
        return disableFields;
    }

    public void setDisableFields(Map<String, List<String>> disableFields) {
        this.disableFields = disableFields;
    }

    public CustomerLayout getLayout() {
        return layout;
    }

    public void setLayout(CustomerLayout layout) {
        this.layout = layout;
    }

    public Map<String, CSPEnum> getEnums() {
        return enums;
    }

    public void setEnums(Map<String, CSPEnum> enums) {
        this.enums = enums;
    }

    public Map<String, CspEntityRestObject> getEntites() {
        return entites;
    }

    public void setEntites(Map<String, CspEntityRestObject> entites) {
        this.entites = entites;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

}
