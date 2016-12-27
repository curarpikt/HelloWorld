package com.chanapp.chanjet.customer.service.cia;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.constant.metadata.UserMetaData;
import com.chanapp.chanjet.customer.reader.CiaReader;
import com.chanapp.chanjet.customer.reader.PortalReader;
import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.util.MsgUtil;
import com.chanapp.chanjet.customer.util.RegexUtil;
import com.chanapp.chanjet.customer.vo.UserQuery;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.customer.vo.VORowSet;
import com.chanapp.chanjet.web.service.BaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanapp.chanjet.web.util.ConvertUtil;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.ccs.api.cia.UserInfo;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.ccs.api.common.Result;
import com.chanjet.csp.ccs.impl.common.HttpUtil;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.util.StringUtils;

public class CiaServiceImpl extends BaseServiceImpl implements CiaServiceItf {
    private final Logger logger = LoggerFactory.getLogger(CiaServiceImpl.class);

    @Override
    public String getOrgFullName(String orgId) {
        Map<String, Object> orgMap = getOrganizationInfoByOrgId(orgId);
        if (orgMap != null)
            return (String) orgMap.get(UserMetaData.orgFullName);
        return null;
    }

    @Override
    public Map<String, Object> getOrganizationInfoByOrgId(String orgId) {
        try {
            Map<String, String> authMap = new HashMap<String, String>();
            authMap.put("appKey", EnterpriseContext.getAppKey());
            authMap.put("appSecret", EnterpriseContext.getAppSecret());
            authMap.put("orgIdentify", orgId);
            String resultJson = HttpUtil.HttpGetAction(CiaReader.getOrgInfoUrl(), authMap);
            Map<String, Object> tokenMap = dataManager.jsonStringToMap(resultJson);
            if (tokenMap.containsKey("errorCode")) {
                tokenMap.put("param", MsgUtil.msgMap.get(tokenMap.get("errorCode")));
            }
            return tokenMap;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("getOrganizationInfoByOrgId exception : ", e);
        }
        return null;
    }
    /*
     * @SuppressWarnings("unchecked") public List<Long> getAppManagerIdFormCIA()
     * throws BOApplicationException { ObjectMapper mapper = new ObjectMapper();
     * List<Long> appSuperUserIds = new ArrayList<Long>(); try { Map<String,
     * String> parameter = new HashMap<String, String>();
     * parameter.put("appKey", EnterpriseContext.getAppKey());
     * parameter.put("access_token", EnterpriseContext.getToken());
     * parameter.put("appId", EnterpriseContext.getAppId()); String resultJson =
     * HttpUtil.HttpGetAction(CiaServiceUrl.getInstance().getAppManagerAddUrl(),
     * parameter); Map<String, Object> retMap = mapper.readValue(resultJson,
     * Map.class); if (retMap != null) { boolean result = (Boolean)
     * retMap.get("result"); if (result) { List<Map<String, Object>> appManagers
     * = (List<Map<String, Object>>) retMap.get("appManagers"); if (appManagers
     * != null) { for (Map<String, Object> appManager : appManagers) { String
     * superUserId = (String) appManager.get("userId"); if
     * (StringUtils.isNotBlank(superUserId)) {
     * appSuperUserIds.add(Long.parseLong(superUserId)); } } } } } } catch
     * (Exception e) { logger.error("getAppManager exception:", e); throw new
     * BOApplicationException("app.common.server.error"); } return
     * appSuperUserIds; }
     */

    /**
     * 判定企业名称是否存在
     * 
     * @param orgName
     * @return
     * @throws Exception
     */
    private Map<String, Object> checkOrgNameExist(String orgName) throws Exception {
        Map<String, String> authMap = new HashMap<String, String>();
        authMap.put("appKey", EnterpriseContext.getAppKey());
        authMap.put("appSecret", EnterpriseContext.getAppSecret());
        Map<String, String> orgNameMap = new HashMap<String, String>();
        orgNameMap.put("orgFullName", orgName);
        authMap.put("orgInfo", dataManager.toJSONString(orgNameMap));
        String resultJson = HttpUtil.HttpGetAction(CiaReader.getExistsOrgNameUrl(), authMap);
        return dataManager.jsonStringToMap(resultJson);
    }

    @Override
    public Map<String, Object> updateOrganizationName(String orgId, String orgName) {
        ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        // 根据企业名称判定企业名称是否存在
        Map<String, Object> resMap = new HashMap<String, Object>();
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            resMap = checkOrgNameExist(orgName);
            // CIA服务出现异常时，返回false。
        } catch (Exception e1) {
            e1.printStackTrace();
            result.put("result", false);
            return result;
        }

        String exists = "1";
        if (resMap.containsKey("exists")) {
            exists = resMap.get("exists").toString();
        }
        if (!"0".equals(exists)) {
            result.put("result", false);
            result.put("errorCode", "111111");
            result.put("param", "企业名称已存在");
            return result;
        }
        // 根据企业ID判定企业是否存在
        Map<String, Object> ciaOrgMap = getOrganizationInfoByOrgId(orgId);
        if (null == ciaOrgMap) {
            result.put("result", false);
            result.put("errorCode", "20204");
            result.put("param", "组织不存在");
            return result;
        }
        if (ciaOrgMap.containsKey("errorCode")) {
            ciaOrgMap.put("param", MsgUtil.msgMap.get(ciaOrgMap.get("errorCode")));
            return ciaOrgMap;
        }
        ciaOrgMap.put("orgName", orgName);
        ciaOrgMap.put("orgFullName", orgName);

        // 根据企业ID、名称变更企业名称
        try {
            Map<String, String> authMap = new HashMap<String, String>();
            authMap.put("appKey", EnterpriseContext.getAppKey());
            authMap.put("access_token", EnterpriseContext.getToken());
            Map<String, Object> jsonObject = new HashMap<String, Object>();
            jsonObject.put("ciaOrgMap", ciaOrgMap);
            authMap.put("orgInfo", dataManager.toJSONString(ciaOrgMap));
            String resultJson = HttpUtil.HttpPostAction(CiaReader.getUpdateOrgInfoUrl(), authMap);
            Map<String, Object> tokenMap = dataManager.jsonStringToMap(resultJson);
            if (tokenMap.containsKey("errorCode")) {
                tokenMap.put("param", MsgUtil.msgMap.get(tokenMap.get("errorCode")));
            }
            return tokenMap;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("updateOrganizationName exception : " + e.getMessage());
        }
        result.put("result", false);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getInviteRecord(int pageno, int pagesize, String auditType) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            Map<String, String> paramter = new HashMap<String, String>();
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("start", String.valueOf((pageno - 1) * pagesize));
            data.put("limit", String.valueOf(pagesize));
            data.put("auditType", auditType);

            paramter.put("appKey", EnterpriseContext.getAppKey());
            paramter.put("access_token", EnterpriseContext.getToken());
            paramter.put("invitationInfo", dataManager.toJSONString(data));
            String url = CiaReader.getInviteRecordUrl();
            String text = HttpUtil.HttpPostAction(url, paramter);
            logger.info("url={}", url);
            Map<String, Object> json = dataManager.jsonStringToMap(text);
            if (json != null && json.containsKey("result")) {
                if (ConvertUtil.toBoolean(json.get("result").toString())) {
                    int total = ConvertUtil.toInt(json.get("count").toString());

                    long allpage = ((total - 1) / pagesize) + 1;
                    if (allpage < pageno) {
                        pageno = (int) allpage;
                        return getInviteRecord(pageno, pagesize, auditType);
                    }
                    List<Map<String, Object>> jsonArr = (List<Map<String, Object>>) json.get("invitationList");
                    int len = jsonArr.size();
                    int i = 0;
                    for (i = 0; i < len; i++) {
                        Map<String, Object> jsonObj = jsonArr.get(i);
                        jsonObj.put("id", jsonObj.get("userId"));
                        jsonArr.set(i, jsonObj);
                    }
                    result.put("pages", allpage);
                    result.put("pageno", pageno);
                    result.put("pagesize", pagesize);
                    result.put("total", total);
                    result.put("items", jsonArr);
                    result.put("authinfo", getAuthInfo());
                    logger.info("result text={}", text);
                    return result;
                }
            }
            logger.info("result text={}", text);
        } catch (Exception e) {
            logger.error("getInviteRecords error", e);
        }
        return null;
    }

    private Map<String, String> getAuthInfo() {
        Map<String, String> result = new HashMap<String, String>();
        try {
            String appId = EnterpriseContext.getAppId();
            logger.info("appid={}", appId);
            Map<String, String> app = new HashMap<String, String>();
            app.put("appId", appId);
            Map<String, String> paramter = new HashMap<String, String>();
            paramter.put("appKey", EnterpriseContext.getAppKey());
            paramter.put("access_token", EnterpriseContext.getToken());
            paramter.put("appAuthroizeInfo", dataManager.toJSONString(app));
            logger.info("invite_record_url={},appkey={},token={}", CiaReader.getAuthLimieUrl(),
                    EnterpriseContext.getAppKey(), EnterpriseContext.getToken());
            String text = HttpUtil.HttpPostAction(CiaReader.getAuthLimieUrl(), paramter);
            if (StringUtils.isNotEmpty(text)) {
                Map<String, Object> resMap = dataManager.jsonStringToMap(text);
                if (resMap != null && resMap.containsKey("result")) {
                    boolean bool = Boolean.valueOf(resMap.get("result").toString());
                    if (bool) {
                        result.put("entSubLincese", resMap.get("entSubLincese").toString());
                        result.put("entAvalibleLincese", resMap.get("entAvalibleLincese").toString());
                        return result;
                    }
                }
            }
            logger.info("result text={}", text);
        } catch (Exception e) {
            logger.error("getAuthInfo error", e);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> records(Integer pageSize, Integer pageNo) {
        ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();
        if (pageNo == 0) {
            pageNo = 1;
        }
        if (pageNo < 0 || pageNo < 1) {
            throw new AppException("app.common.params.invalid");
        }
        String auditType = "2";
        Map<String, Object> data = getInviteRecord(pageNo, pageSize, auditType);
        if (data != null) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
            if (items != null) {
                List<Long> userIds = new ArrayList<Long>();
                for (int i = 0; i < items.size(); i++) {
                    Map<String, Object> item = items.get(i);
                    Long userId = Long.valueOf(item.get("userId") + "");
                    userIds.add(userId);
                }
                if (userIds.size() > 0) {
                    UserQuery query = new UserQuery();
                    query.setUserIds(userIds);
                    VORowSet<UserValue> users = ServiceLocator.getInstance().lookup(UserServiceItf.class)
                            .getUsersByParam(query);
                    for (int i = 0; i < items.size(); i++) {
                        Map<String, Object> item = items.get(i);
                        Long userId = Long.valueOf(item.get("userId") + "");
                        boolean flag = false;
                        for (UserValue user : users.getItems()) {
                            if (userId.equals((Long) user.getUserId())) {
                                // item.put("firstLoginDate",
                                // user.getFieldValue(SC.createdDate));
                                item.put("firstLoginStatus", true);
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            item.put("firstLoginStatus", false);
                        }
                    }
                }
            }
            return data;
        }

        throw new AppException("app.common.server.error");
    }

    @Override
    public Map<String, Object> send(String data,BoSession session) {
        // 判定当前用户是否是老板
        ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        // 获取当前企业的企业名称。（企业名称不能为空）
        Map<String, Object> orgMap = getOrganizationInfoByOrgId(EnterpriseContext.getCurrentUser().getOrgId());
        if (orgMap != null && orgMap.containsKey("result")) {
            Boolean result = (Boolean) orgMap.get("result");
            if (result) {
                String orgName = (String) orgMap.get("orgName");
                if (StringUtils.isEmpty(orgName)) {
                    throw new AppException("app.invite.orgName.empty");
                }
            }
        }
        logger.info("data={}", data);
        // 解析data -> json对象，获取被邀请人信息。（如果被邀请人信息为空，或非法邮箱、非法手机号码则报错）
        Map<String, Object> json = dataManager.jsonStringToMap(data);
        String account = "";
        if (json != null && json.containsKey("account")) {
            account = (String) json.get("account");
        }
        boolean isAccount = true;
        if (StringUtils.isEmpty(account)) {
            isAccount = false;
        } else if (!RegexUtil.validEmail(account) && !RegexUtil.validSimpleMobile(account)) {
            isAccount = false;
        }
        if (!isAccount) {
            throw new AppException("app.invite.params.invalid");
        }

        // 调用WEB邀请服务，并返回结果（是否成功）
        boolean bool = inviteSend(account,session);
        if (bool) {
            return new HashMap<String, Object>();
        }
        throw new AppException("app.common.server.error");
    }

    /**
     * 根据被邀请人信息设置inviteUserValue对象
     * 
     * @param data 被邀请人信息（json字符串）
     * @param inviteUserValue
     */
    private void setInviteUserValue(String account, InviteUserValue inviteUserValue) {

        inviteUserValue.setUserIdentifyList(account);
        Map<String, String> names = new HashMap<String, String>();
        if (account.contains("@")) {
            names.put(account, account.substring(0, account.indexOf("@")));
        } else {
            names.put(account, account);
        }
        // 将被邀请人信息设置到inviteUserValue
        inviteUserValue.setIdentifyNameList(names);

        String url = PortalReader.getDomain();
        inviteUserValue.setNewEmailUrl(url + "/login?app=customer");
        inviteUserValue.setNewMobileUrl(url + "/chanjet/customer");
        inviteUserValue.setExistsEmailUrl(url + "/login?app=customer");
        inviteUserValue.setExistsMobileUrl(url + "/chanjet/customer");
        inviteUserValue.setExistsEntInternalEmailUrl(url + "/login?app=customer");
        inviteUserValue.setExistsEntInternalMobileUrl(url + "/chanjet/customer");

        inviteUserValue.setAppId(EnterpriseContext.getAppId());
        inviteUserValue.setNeedNotifyEntInternalInvite("1");
    }

    /**
     * 短链邀请用户
     */
    private Result<String> invitationUserWithShortUrl(InviteUserValue inviteUserValue) {
        Result<String> result = new Result<String>();
        try {
            Map<String, String> paramter = new HashMap<String, String>();
            paramter.put("appKey", EnterpriseContext.getAppKey());
            paramter.put("access_token", EnterpriseContext.getToken());
            paramter.put("inviteUserInfo", dataManager.toJSONString(inviteUserValue));
            logger.info("inviteUserInfo:" + paramter.get("inviteUserInfo"));
            String resultJson = HttpUtil.HttpPostAction(CiaReader.getShortInvitationUrl(), paramter);
            result.setInfo(resultJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private UserInfo _getUser(String jsonStr) {
        UserInfo user = new UserInfo();
        Map<String, Object> mapObj = dataManager.jsonStringToMap(jsonStr);
        user.setAddress((String) mapObj.get("address"));
        user.setBirthday((String) mapObj.get("birthday"));
        user.setCreateTime((String) mapObj.get("createTime"));
        user.setDefaultOrganization((String) mapObj.get("defaultOrganization"));
        user.setDepartment((String) mapObj.get("departmentName"));
        user.setEducation((String) mapObj.get("education"));
        user.setEmail((String) mapObj.get("email"));
        user.setFax((String) mapObj.get("fax"));
        user.setGraduateSchool((String) mapObj.get("graduateSchool"));
        user.setGraduationYear((String) mapObj.get("graduationYear"));
        user.setHeadPicture((String) mapObj.get("headPicture"));
        user.setHobby((String) mapObj.get("hobby"));
        user.setIdCard((String) mapObj.get("idCard"));
        user.setInstitute((String) mapObj.get("institute"));
        user.setJointime((String) mapObj.get("jointime"));
        user.setMajor((String) mapObj.get("major"));
        user.setMobile((String) mapObj.get("mobile"));
        user.setModifyTime((String) mapObj.get("modifyTime"));
        user.setMsn((String) mapObj.get("msn"));
        user.setName((String) mapObj.get("name"));
        user.setNickName((String) mapObj.get("nikeName"));
        user.setOfficePhone((String) mapObj.get("officePhone"));
        user.setOrgId((String) mapObj.get("orgId"));
        user.setOrgName((String) mapObj.get("orgName"));
        user.setOrgType(Integer.parseInt((String) mapObj.get("orgType")));
        user.setOrigin((String) mapObj.get("origin"));
        user.setPosition((String) mapObj.get("position"));
        user.setQq((String) mapObj.get("qq"));
        user.setReceiveAddress((String) mapObj.get("receiveAddress"));
        user.setRemark((String) mapObj.get("remark"));
        user.setSex((String) mapObj.get("sex"));
        user.setSignature((String) mapObj.get("signature"));
        user.setUserChanjetId((String) mapObj.get("userChanjetId"));
        user.setUserId((String) mapObj.get("userId"));
        user.setUserLongId(Long.parseLong(mapObj.get("userId").toString()));
        user.setUsername((String) mapObj.get("username"));
        user.setWebsite((String) mapObj.get("website"));
        user.setZipCode((String) mapObj.get("zipCode"));
        return user;
    }

    private Result<UserInfo> getUserInfo(String userId) {
        Result<UserInfo> result = new Result<UserInfo>();
        try {
            Map<String, String> authMap = new HashMap<String, String>();
            authMap.put("appKey", EnterpriseContext.getAppKey());
            authMap.put("appSecret", EnterpriseContext.getAppSecret());
            authMap.put("userIdentify", userId);
            String resultJson = HttpUtil.HttpGetAction(CiaReader.getUserInfoByIdUrl(), authMap);
            if (resultJson.contains("\"errorCode\"")) {
                Map<String, Object> tokenMap = dataManager.jsonStringToMap(resultJson);
                result.setCode((String) tokenMap.get("errorCode"));
            } else {
                UserInfo user = _getUser(resultJson);
                result.setInfo(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private boolean inviteSend(String account,BoSession session) {
        InviteUserValue inviteUserValue = new InviteUserValue();
        setInviteUserValue(account, inviteUserValue);

        Result<String> result = invitationUserWithShortUrl(inviteUserValue);
        String info = result.getInfo();
        logger.info("invite result={}", info);
        boolean bool = false;

        try {
            Map<String, Object> resultMap = dataManager.jsonStringToMap(info);
            if (resultMap != null && resultMap.containsKey("result")) {
                bool = (Boolean) resultMap.get("result");
                // 邀请成功
                if (bool) {
                    Map<String, Object> inviteRes = dataManager
                            .jsonStringToMap(resultMap.get("inviteResult").toString());
                    Map<String, Object> jsonObj = (Map<String, Object>) inviteRes.get(account);
                    logger.info("result account info={}", jsonObj);
                    if (jsonObj != null) {
                        if (jsonObj.containsKey("errorCode")) {
                            if ("20139".equals(jsonObj.get("errorCode").toString())) {
                                throw new AppException("app.invite.user.notactive");
                            }
                        } else {
                            String flag = jsonObj.get("flag").toString();
                            long userId = ConvertUtil.toLong(jsonObj.get("userId").toString());
                            if ("3"/* USER_EXIST */.equals(flag)) {
                            	Result<Boolean> authInfo = new com.chanjet.csp.ccs.impl.cia.CiaServiceImpl()
                                        .authUser(String.valueOf(userId), EnterpriseContext.getAppId());
                                if ("true".equals(authInfo.getInfo())||authInfo.getInfo()) {
                                    Result<UserInfo> userResult = getUserInfo(String.valueOf(userId));

                                    ServiceLocator.getInstance().lookup(UserServiceItf.class)
                                            .initOtherUser(userResult.getInfo(),session);
                                } else if ("20311".equals(authInfo.getCode())) {
                                    throw new AppException("app.invite.user.exists");
                                } else {
                                    throw new AppException("app.invite.user.full");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("invite send error", e);
        }
        return bool;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> inviteAuthShort(InviteUserValue inviteUserValue,BoSession session) {
        Map<String, Object> temp = new HashMap<String, Object>();
        Result<String> result = invitationUserWithShortUrl(inviteUserValue);
        String info = result.getInfo();
        try {
            Map<String, Object> resultMap = dataManager.jsonStringToMap(info);
            Boolean success = (Boolean) resultMap.get("result");
            if (success) {
                logger.info("success:" + resultMap);
                temp.put("result", success);
                String inviteResult = (String) resultMap.get("inviteResult");
                Map<String, Object> inviteResultMap = dataManager.jsonStringToMap(inviteResult);
                Iterator<Map.Entry<String, Object>> it = inviteResultMap.entrySet().iterator();
                Object[] mapArray = new Object[inviteResultMap.size()];
                int i = 0;
                while (it.hasNext()) {
                    Map.Entry<String, Object> entry = it.next();
                    Map<String, Object> value = (Map<String, Object>) entry.getValue();
                    Object errorCode = value.get("errorCode");
                    if (null == errorCode) {
                        Object flag = value.get("flag");
                        String userId = value.get("userId") + "";
                        if ("3"/* USER_EXIST */.equals(flag)) {
                        	Result<Boolean> authInfo = new com.chanjet.csp.ccs.impl.cia.CiaServiceImpl()
                                    .authUser(String.valueOf(userId), EnterpriseContext.getAppId());
                            if ("true".equals(authInfo.getInfo())) {
                                Result<UserInfo> userResult = getUserInfo(String.valueOf(userId));
                                ServiceLocator.getInstance().lookup(UserServiceItf.class)
                                        .initOtherUser(userResult.getInfo(),session);
                            } else if ("20311".equals(authInfo.getCode())) {
                                throw new AppException("app.invite.user.exists");
                            } else {
                                throw new AppException("app.invite.user.full");
                            }
                        }
                    }
                    value.put("inviteKey", entry.getKey());
                    mapArray[i] = value;
                    i++;
                }
                temp.put("inviteResult", mapArray);
            } else {
                logger.info("fail:" + resultMap);
                temp.putAll(resultMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }

    @Override
    public Map<String, Object> inviteShort(String data,BoSession session) {
        ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        InviteUserValue user = dataManager.fromJSONString(data, InviteUserValue.class);

        Map<String, Object> orgMap = getOrganizationInfoByOrgId(EnterpriseContext.getCurrentUser().getOrgId());
        if (orgMap != null && orgMap.containsKey("result")) {
            Boolean result = (Boolean) orgMap.get("result");
            if (result) {
                String orgName = (String) orgMap.get("orgName");
                if (StringUtils.isEmpty(orgName)) {
                    throw new AppException("app.invite.orgName.empty");
                }
            }
        }
        user.setAppId(EnterpriseContext.getAppId());
        return inviteAuthShort(user,session);
    }

    @Override
    public Map<String, Object> findUserByIdentify(String identify) {
        try {
            Map<String, String> parameter = new HashMap<String, String>();
            Map<String, Object> jsonObject = new HashMap<String, Object>();
            jsonObject.put("userIdentify", identify);
            jsonObject.put("appKey", EnterpriseContext.getAppKey());
            parameter.put("userInfo", dataManager.toJSONString(jsonObject));
            String resultJson = HttpUtil.HttpPostAction(
                    CiaReader.getUserIdentifyUrl() + "?appKey=" + EnterpriseContext.getAppKey(), parameter);
            return dataManager.jsonStringToMap(resultJson);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("app.common.server.error");
        }
    }

    @Override
    public Map<String, Object> sendBindingActiveEmailOrMobile(String userId, String identify, String activeWay) {
        try {
            Map<String, String> parameter = new HashMap<String, String>();
            Map<String, Object> jsonObject = new HashMap<String, Object>();
            jsonObject.put("userId", userId);
            jsonObject.put("activeWay", activeWay);
            jsonObject.put("activeType", "3");// 固定值为3， 3表示进行绑定操作；
            jsonObject.put("mobile", identify);
            // jsonObject.put("email",null);
            parameter.put("sendInfo", dataManager.toJSONString(jsonObject));
            String resultJson = HttpUtil.HttpPostAction(
                    CiaReader.getSendBindingMsgUrl() + "?appKey=" + EnterpriseContext.getAppKey(), parameter);
            return dataManager.jsonStringToMap(resultJson);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("app.common.server.error");
        }
    }

    @Override
    public Map<String, Object> findOrgPartnerAppRelationList(String orgId) {
        try {
            Map<String, String> parameter = new HashMap<String, String>();
            if (StringUtils.isNotEmpty(orgId)) {
                parameter.put("orgId", orgId);
            }
            parameter.put("appKey", EnterpriseContext.getAppKey());
            parameter.put("access_token", EnterpriseContext.getToken());
            parameter.put("appId", EnterpriseContext.getAppId());
            String resultJson = HttpUtil.HttpGetAction(CiaReader.getFindOrgPartnerUrl(), parameter);
            return dataManager.jsonStringToMap(resultJson);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("app.common.server.error");
        }
    }

    @Override
    public Map<String, Object> fingPartnerInfoByUsername(String partnerId) {
        try {
            Map<String, String> parameter = new HashMap<String, String>();
            parameter.put("username", partnerId);
            String resultJson = HttpUtil.HttpPostAction(CiaReader.getFindPartnerUrl() + "?appKey="
                    + EnterpriseContext.getAppKey() + "&appSecret=" + EnterpriseContext.getAppSecret(), parameter);
            return dataManager.jsonStringToMap(resultJson);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("app.common.server.error");
        }
    }

    @Override
    public Map<String, Object> bindingOrgPartnerAppRelation(String orgId, String partnerId) {
        try {
            Map<String, String> parameter = new HashMap<String, String>();
            Map<String, Object> jsonObject = new HashMap<String, Object>();
            if (StringUtils.isNotEmpty(orgId)) {
                jsonObject.put("orgId", orgId);
            }
            jsonObject.put("agentCode", partnerId);
            jsonObject.put("appId", EnterpriseContext.getAppId());
            parameter.put("organizationPartnerAppInfo", dataManager.toJSONString(jsonObject));
            String resultJson = HttpUtil.HttpPostAction(CiaReader.getBindingOrgPartnerUrl() + "?appKey="
                    + EnterpriseContext.getAppKey() + "&access_token=" + EnterpriseContext.getToken(), parameter);
            return dataManager.jsonStringToMap(resultJson);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("app.common.server.error");
        }
    }

    @Override
    public Map<String, Object> activeBindingEmailOrMobile(String userId, String activeCode, String activeWay,
            String activeType) {
        try {
            Map<String, String> parameter = new HashMap<String, String>();
            Map<String, Object> jsonObject = new HashMap<String, Object>();
            jsonObject.put("userId", userId);
            jsonObject.put("activeWay", activeWay);
            jsonObject.put("activeType", activeType);// 3表示进行绑定操作；4解绑
            jsonObject.put("activeCode", activeCode);

            parameter.put("activeInfo", dataManager.toJSONString(jsonObject));
            String resultJson = HttpUtil.HttpPostAction(
                    CiaReader.getBindingUserIdentifyUrl() + "?appKey=" + EnterpriseContext.getAppKey(), parameter);
            return dataManager.jsonStringToMap(resultJson);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("app.common.server.error");
        }
    }

    @Override
    public Map<String, Object> resetThridPlatformUserPwd(String pwd, String passwordLevel) {
        try {
            Map<String, String> parameter = new HashMap<String, String>();
            Map<String, Object> jsonObject = new HashMap<String, Object>();
            jsonObject.put("newPassword", pwd);
            if (StringUtils.isEmpty(passwordLevel)) {
                passwordLevel = "3";
            }
            jsonObject.put("passwordLevel", passwordLevel);

            parameter.put("passwordInfo", dataManager.toJSONString(jsonObject));
            String resultJson = HttpUtil.HttpPostAction(CiaReader.getThirdRestPWDUrl() + "?appKey="
                    + EnterpriseContext.getAppKey() + "&access_token=" + EnterpriseContext.getToken(), parameter);
            return dataManager.jsonStringToMap(resultJson);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("resetThridPlatformUserPwd exception:", e);
            throw new AppException("app.common.server.error");
        }
    }

    @Override
    public Map<String, Object> getUserInfoFromCia() {
        try {
            Map<String, String> parameter = new HashMap<String, String>();
            parameter.put("appKey", EnterpriseContext.getAppKey());
            parameter.put("access_token", EnterpriseContext.getToken());
            parameter.put("needOrgLists", "1");
            String resultJson = HttpUtil.HttpGetAction(CiaReader.getUserInfoUrl(), parameter);
            return dataManager.jsonStringToMap(resultJson);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("app.common.server.error");
        }
    }

    @Override
    public Map<String, Object> updateHeadPicture(File headPic) {
        try {
/*            Map<String, String> header = new HashMap<String, String>();
            String boundary = "------------upload_file_" + System.currentTimeMillis();
            header.put("Content-Type", "multipart/form-data; boundary=" + boundary);

            StringBuffer strBuf = new StringBuffer();

            String contentType = URLConnection.guessContentTypeFromName(headPic.getName());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            strBuf.append("\r\n").append("--").append(boundary).append("\r\n");
            strBuf.append("Content-Disposition: form-data; name=\"file1\"; filename=\"" + headPic.getName() + "\"\r\n");
            strBuf.append("Content-Type:" + contentType + "\r\n\r\n");

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            bout.write(strBuf.toString().getBytes());

            DataInputStream in = null;
            try {
                in = new DataInputStream(new FileInputStream(headPic));
                int bytes = 0;
                byte[] bufferOut = new byte[1024];
                while ((bytes = in.read(bufferOut)) != -1) {
                    bout.write(bufferOut, 0, bytes);
                }
            } finally {
                if (in != null) {
                    in.close();
                }
            }

            bout.write(("\r\n--" + boundary + "--\r\n").getBytes());
            byte[] body = bout.toByteArray();

            
            com.chanapp.chanjet.customer.http.HttpResponse resultJson = com.chanapp.chanjet.customer.util.HttpUtil
                    .doPost(CiaReader.getUserHeadPictrueUrl() + "?appKey=" + EnterpriseContext.getAppKey()
                            + "&access_token=" + EnterpriseContext.getToken(), header, body, 10000);*/
            // .HttpAction( HttpUtil.POST, CiaReader.getUserHeadPictrueUrl() +
            // "?appKey=" + EnterpriseContext.getAppKey()
            // + "&access_token=" + EnterpriseContext.getToken(),
            // null, null, null, headPic.getName(), headPic);
    		String retstr=null;
    		PostMethod postMethod = null;
    		HttpClient httpClient = new HttpClient();
			postMethod = new PostMethod(CiaReader.getUserHeadPictrueUrl() +"?appKey=" + EnterpriseContext.getAppKey() + "&access_token=" + EnterpriseContext.getToken());
			Part[] parts = { new FilePart(headPic.getName(), headPic)};
			postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));			
			httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(10000);  
			httpClient.executeMethod(postMethod);
			retstr= postMethod.getResponseBodyAsString();
            return dataManager.jsonStringToMap(retstr);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("app.common.server.error");
        }

    }

    @Override
    public Map<String, Object> addAppManager(Long userId) throws AppException {
        try {
            Map<String, String> parameter = new HashMap<String, String>();
            Map<String, Object> jsonObject = new HashMap<String, Object>();
            jsonObject.put("userId", userId + "");
            jsonObject.put("appId", EnterpriseContext.getAppId());
            parameter.put("appManagerInfo", dataManager.toJSONString(jsonObject));
            String resultJson = HttpUtil.HttpPostAction(CiaReader.getAppManagerAddUrl() + "?appKey="
                    + EnterpriseContext.getAppKey() + "&access_token=" + EnterpriseContext.getToken(), parameter);
            return dataManager.jsonStringToMap(resultJson);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("app.common.server.error");
        }
    }

    @Override
    public Map<String, Object> cancelAppManager(Long userId) throws AppException {
        try {
            Map<String, String> parameter = new HashMap<String, String>();
            Map<String, Object> jsonObject = new HashMap<String, Object>();
            jsonObject.put("userId", userId + "");
            jsonObject.put("appId", EnterpriseContext.getAppId());
            parameter.put("appManagerInfo", dataManager.toJSONString(jsonObject));
            String resultJson = HttpUtil.HttpPostAction(CiaReader.getAppManagerCancelUrl() + "?appKey="
                    + EnterpriseContext.getAppKey() + "&access_token=" + EnterpriseContext.getToken(), parameter);
            return dataManager.jsonStringToMap(resultJson);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("app.common.server.error");
        }
    }
}
