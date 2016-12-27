package com.chanapp.chanjet.customer.service.sync;

public class SyncUserInfo {
    public Long getLastSyncVersion() {
        return lastSyncVersion;
    }

    public void setLastSyncVersion(Long lastSyncVersion) {
        this.lastSyncVersion = lastSyncVersion;
    }

    private String MD5Content;
    private Long lastSyncVersion;

    public String getMD5Content() {
        return MD5Content;
    }

    public void setMD5Content(String mD5Content) {
        MD5Content = mD5Content;
    }

}
