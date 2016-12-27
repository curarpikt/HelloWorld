package com.chanapp.chanjet.customer.reader;

import com.chanapp.chanjet.web.reader.PropertiesReader;

/**
 * CIA相关配置
 * 
 * @author tds
 *
 */
public class CiaReader {
    final static PropertiesReader reader = PropertiesReader.getInstance("customer/cia.properties");

    public static String getDomain() {
        return reader.getString("cia.domain");
    }

    public static String getAppAuthroizeForEntUserUrl() {
        return getDomain() + reader.getString("cia.url.getAppAuthroizeForEntUser");
    }

    public static String getOrganizationUrl() {
        return getDomain() + reader.getString("cia.url.organization");
    }

    public static String getUserHeadPictrueUrl() {
        return getDomain() + reader.getString("cia.url.userHeadPictrue");
    }

    public static String getUpdateOrgInfoUrl() {
        return getDomain() + reader.getString("update_orginfo");
    }

    /**
     * 获取ExistsOrgName服务的url
     * 
     * @return UpdateOrgInfoUrl
     */
    public static String getExistsOrgNameUrl() {
        return getDomain() + reader.getString("exists_orgname");
    }

    /**
     * 获取OrgInfo服务的url
     * 
     * @return UpdateOrgInfoUrl
     */
    public static String getOrgInfoUrl() {
        return getDomain() + reader.getString("orginfo");
    }

    /**
     * 获取UserInfoById服务的url
     * 
     * @return UpdateOrgInfoUrl
     */
    public static String getUserInfoByIdUrl() {
        return getDomain() + reader.getString("get_userinfo_byId");
    }

    /**
     * 获取短信邀请服务的url
     * 
     * @return invite_record
     */
    public static String getShortInvitationUrl() {
        return getDomain() + reader.getString("short_invitation");
    }

    /**
     * 获取邀请列表服务的url
     * 
     * @return invite_record
     */
    public static String getInviteRecordUrl() {
        return getDomain() + reader.getString("invite_record");
    }

    /**
     * 获取邀请授权人数服务的url
     * 
     */
    public static String getAuthLimieUrl() {
        return getDomain() + reader.getString("auth_limie");
    }

    /**
     * orgusers
     * 
     */
    public static String getOrgUsersUrl() {
        return getDomain() + reader.getString("orgusers");
    }

    /**
     * userinfo
     * 
     */
    public static String getUserInfoUrl() {
        return getDomain() + reader.getString("userinfo");
    }

    /**
     * find_useridentify
     * 
     */
    public static String getUserIdentifyUrl() {
        return getDomain() + reader.getString("find_useridentify");
    }

    /**
     * binding_sendmsg
     * 
     */
    public static String getSendBindingMsgUrl() {
        return getDomain() + reader.getString("binding_sendmsg");
    }

    /**
     * find_partner
     * 
     */
    public static String getFindPartnerUrl() {
        return getDomain() + reader.getString("find_partner");
    }

    /**
     * find_org_partner
     * 
     */
    public static String getFindOrgPartnerUrl() {
        return getDomain() + reader.getString("find_org_partner");
    }

    /**
     * binding_org_partner
     * 
     */
    public static String getBindingOrgPartnerUrl() {
        return getDomain() + reader.getString("binding_org_partner");
    }

    /**
     * binding_useridentify
     * 
     */
    public static String getBindingUserIdentifyUrl() {
        return getDomain() + reader.getString("binding_useridentify");
    }

    /**
     * url_appmanager_add
     * 
     */
    public static String getAppManagerAddUrl() {
        return getDomain() + reader.getString("url_appmanager_add");
    }

    /**
     * url_appmanager_cancel
     * 
     */
    public static String getAppManagerCancelUrl() {
        return getDomain() + reader.getString("url_appmanager_cancel");
    }

    public static String getThirdRestPWDUrl() {
        return getDomain() + reader.getString("third_restpwd");
    }

    public static String getEveryLoginApp() {
        return getDomain() + reader.getString("every_login");
    }

    public static String getFirstLoginApp() {
        return getDomain() + reader.getString("first_login");
    }

    public static String getUserHeadPictureUrl() {
        return getDomain() + reader.getString("user_head_picture");
    }
}
