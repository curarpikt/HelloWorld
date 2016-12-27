package com.chanapp.chanjet.customer.cache;

import java.util.Date;

public class VersionInfo {

    static VersionInfo version;

    private Date lastModifiedDate = new Date();

    private VersionInfo() {
    }

    public static VersionInfo getInstance() {
        if (version == null) {
            version = new VersionInfo();
            return version;
        }
        return version;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date date) {
        lastModifiedDate = date;
    }

}
