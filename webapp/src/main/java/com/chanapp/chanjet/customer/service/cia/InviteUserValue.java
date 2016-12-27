package com.chanapp.chanjet.customer.service.cia;

import java.util.Map;

public class InviteUserValue {

    public String getUserIdentifyList() {
        return userIdentifyList;
    }

    public void setUserIdentifyList(String userIdentifyList) {
        this.userIdentifyList = userIdentifyList;
    }

    public String getNewEmailUrl() {
        return newEmailUrl;
    }

    public void setNewEmailUrl(String newEmailUrl) {
        this.newEmailUrl = newEmailUrl;
    }

    public String getNewMobileUrl() {
        return newMobileUrl;
    }

    public void setNewMobileUrl(String newMobileUrl) {
        this.newMobileUrl = newMobileUrl;
    }

    public String getnEmailUrl() {
        return nEmailUrl;
    }

    public void setnEmailUrl(String nEmailUrl) {
        this.nEmailUrl = nEmailUrl;
        this.setNewEmailUrl(nEmailUrl);
    }

    public String getnMobileUrl() {
        return nMobileUrl;
    }

    public void setnMobileUrl(String nMobileUrl) {
        this.nMobileUrl = nMobileUrl;
        this.setNewMobileUrl(nMobileUrl);
    }

    public String getExistsEmailUrl() {
        return existsEmailUrl;
    }

    public void setExistsEmailUrl(String existsEmailUrl) {
        this.existsEmailUrl = existsEmailUrl;
    }

    public String getExistsMobileUrl() {
        return existsMobileUrl;
    }

    public void setExistsMobileUrl(String existsMobileUrl) {
        this.existsMobileUrl = existsMobileUrl;
    }

    public String getExistsEntInternalEmailUrl() {
        return existsEntInternalEmailUrl;
    }

    public void setExistsEntInternalEmailUrl(String existsEntInternalEmailUrl) {
        this.existsEntInternalEmailUrl = existsEntInternalEmailUrl;
    }

    public String getExistsEntInternalMobileUrl() {
        return existsEntInternalMobileUrl;
    }

    public void setExistsEntInternalMobileUrl(String existsEntInternalMobileUrl) {
        this.existsEntInternalMobileUrl = existsEntInternalMobileUrl;
    }

    public String getNeedNotifyEntInternalInvite() {
        return needNotifyEntInternalInvite;
    }

    public void setNeedNotifyEntInternalInvite(String needNotifyEntInternalInvite) {
        // this.needNotifyEntInternalInvite = needNotifyEntInternalInvite;
        if (null == needNotifyEntInternalInvite || "".equals(needNotifyEntInternalInvite.trim())
                || "0".equals(needNotifyEntInternalInvite)) {
            this.needNotifyEntInternalInvite = "0";
        } else {
            this.needNotifyEntInternalInvite = "1";
        }
    }

    private String userIdentifyList;
    private String newEmailUrl;
    private String newMobileUrl;
    // 兼容IOS,IOS属性名开头不能是new
    private String nEmailUrl;
    private String nMobileUrl;
    private String existsEmailUrl;
    private String existsMobileUrl;
    private String existsEntInternalEmailUrl;
    private String existsEntInternalMobileUrl;
    private String needNotifyEntInternalInvite;
    private Map<String, String> identifyNameList;
    private String appId;

    /**
     * @return the appId
     */
    public String getAppId() {
        return appId;
    }

    /**
     * @param appId the appId to set
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * @return the identifyNameList
     */
    public Map<String, String> getIdentifyNameList() {
        return identifyNameList;
    }

    /**
     * @param identifyNameList the identifyNameList to set
     */
    public void setIdentifyNameList(Map<String, String> identifyNameList) {
        this.identifyNameList = identifyNameList;
    }

}
