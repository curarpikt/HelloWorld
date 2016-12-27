package com.chanapp.chanjet.customer.reader;

import com.chanapp.chanjet.web.reader.PropertiesReader;

/**
 * 官网相关配置
 * 
 * @author tds
 *
 */
public class PortalReader {
    final static PropertiesReader reader = PropertiesReader.getInstance("customer/portal.properties");

    public static String getDomain() {
        return reader.getString("portal.domain");
    }

    public static String getOrgListUrl() {
        return getDomain() + reader.getString("portal.url.orgList");
    }

    public static String getChangeOrgUrl() {
        return getDomain() + reader.getString("portal.url.changeOrg");
    }

    public static String getFindOrgInfoByOrgIdUrl() {
        return getDomain() + reader.getString("portal.url.findOrgInfoByOrgId");
    }

    public static String getFindOrgInfoByOrgAccountUrl() {
        return getDomain() + reader.getString("portal.url.findOrgInfoByOrgAccount");
    }

    public static String getUpdateVersionUrl() {
        return getDomain() + reader.getString("portal.url.updateVersion");
    }
}
