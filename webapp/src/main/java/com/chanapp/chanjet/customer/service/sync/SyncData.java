package com.chanapp.chanjet.customer.service.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SyncData {

    private Long syncVersion;
    private String type;
    private String fileName;
    private String unzipPass;
    private Boolean hierarchyChange;
    private List<Map<String, Object>> hierarchyUsers;

    public Long getSyncVersion() {
        return syncVersion;
    }

    public void setSyncVersion(Long syncVersion) {
        this.syncVersion = syncVersion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUnzipPass() {
        return unzipPass;
    }

    public void setUnzipPass(String unzipPass) {
        this.unzipPass = unzipPass;
    }

    public Boolean getHierarchyChange() {
        return hierarchyChange;
    }

    public void setHierarchyChange(Boolean hierarchyChange) {
        this.hierarchyChange = hierarchyChange;
    }

    public List<Map<String, Object>> getHierarchyUsers() {
        if (hierarchyUsers == null)
            return new ArrayList<Map<String, Object>>();
        return hierarchyUsers;
    }

    public void setHierarchyUsers(List<Map<String, Object>> hierarchyUsers) {
        this.hierarchyUsers = hierarchyUsers;
    }

}
