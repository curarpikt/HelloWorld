package com.chanapp.chanjet.customer.service.binding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.service.cia.CiaServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.MsgUtil;
import com.chanapp.chanjet.web.service.BaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.util.StringUtils;

public class BindingServiceImpl extends BaseServiceImpl implements BindingServiceItf {

    @Override
    public Map<String, Object> sendBindingMobileMsg(String mobile) {
        if (!StringUtils.hasLength(mobile)) {
            throw new AppException("app.bindingMobile.mobile.isnull");
        }
        Map<String, Object> userInfo = ServiceLocator.getInstance().lookup(CiaServiceItf.class)
                .findUserByIdentify(mobile);
        if (userInfo.containsKey("errorCode")) {
            String errorCode = userInfo.get("errorCode") + "";
            userInfo.put("errorMsg", MsgUtil.getMsg(errorCode));
        } else {
            Boolean result = (Boolean) userInfo.get("result");
            String exists = (String) userInfo.get("exists");
            if (result == true && "0".equals(exists)) {
                Map<String, Object> resultMsg = ServiceLocator.getInstance().lookup(CiaServiceItf.class)
                        .sendBindingActiveEmailOrMobile(EnterpriseContext.getCurrentUser().getUserId(), mobile, "2");
                if (resultMsg.containsKey("errorCode")) {
                    String errorCode = resultMsg.get("errorCode") + "";
                    resultMsg.put("errorMsg", MsgUtil.getMsg(errorCode));
                }
                return resultMsg;
            }
        }
        return userInfo;
    }

    @Override
    public Map<String, Object> getOrgBindingPartner(String orgId) {
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> orgPartnerResult = ServiceLocator.getInstance().lookup(CiaServiceItf.class)
                .findOrgPartnerAppRelationList(orgId);
        Boolean reqFlag = (Boolean) orgPartnerResult.get("result");
        if (reqFlag != true || orgPartnerResult.containsKey("errorCode")) {
            String errorCode = orgPartnerResult.get("errorCode") + "";
            orgPartnerResult.put("errorMsg", MsgUtil.getMsg(errorCode));
            return orgPartnerResult;
        } else {
            result.put("result", true);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orgPartnerInfo = (List<Map<String, Object>>) orgPartnerResult
                    .get("OrganizationPartnerAppInfos");
            if (orgPartnerInfo == null || orgPartnerInfo.size() < 1) {
                result.put("partnerBinding", "0");// 没有绑定
            } else {
                result.put("partnerBinding", "1");// 已绑定绑定
                Map<String, Object> partner = orgPartnerInfo.get(0);
                result.put("partnerId", partner.get("agentCode"));
                String partnerFullName = (String) partner.get("partnerFullName");
                if (StringUtils.isEmpty(partnerFullName)) {
                    partnerFullName = (String) partner.get("partnerName");
                }
                result.put("partnerFullName", partnerFullName);
                result.put("partnerLevel", partner.get("partnerLevel"));
                result.put("partnerAddress", partner.get("partnerAddress"));
                result.put("orgId", partner.get("orgId"));
                result.put("orgFullName", partner.get("orgFullName"));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getPartnerInfoById(String partnerId) {
        if (!StringUtils.hasLength(partnerId)) {
            throw new AppException("app.bindingPartner.partnerId.isnull");
        }
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> partnerResult = ServiceLocator.getInstance().lookup(CiaServiceItf.class)
                .fingPartnerInfoByUsername(partnerId);
        Boolean reqFlag = (Boolean) partnerResult.get("result");
        if (reqFlag != true || partnerResult.containsKey("errorCode")) {
            String errorCode = partnerResult.get("errorCode") + "";
            partnerResult.put("errorMsg", MsgUtil.getMsg(errorCode));
            return partnerResult;
        } else {
            result.put("result", true);
            List<Map<String, Object>> partnerInfo = (List<Map<String, Object>>) partnerResult.get("partnerInfoArray");
            if (partnerInfo != null && partnerInfo.size() > 0) {
                Map<String, Object> partner = partnerInfo.get(0);
                result.put("partnerId", partnerId);
                String partnerFullName = (String) partner.get("orgFullName");
                if (StringUtils.isEmpty(partnerFullName)) {
                    partnerFullName = (String) partner.get("orgName");
                }
                result.put("partnerFullName", partnerFullName);
                result.put("partnerLevel", partner.get("partnerLevel"));
                result.put("partnerAddress", partner.get("address"));
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> bindingOrgPartner(String orgId, String partnerId) {
        if (!StringUtils.hasLength(partnerId)) {
            throw new AppException("app.bindingPartner.partnerId.isnull");
        }
        Map<String, Object> partnerInfo = ServiceLocator.getInstance().lookup(CiaServiceItf.class)
                .bindingOrgPartnerAppRelation(orgId, partnerId);
        if (partnerInfo.containsKey("errorCode")) {
            String errorCode = partnerInfo.get("errorCode") + "";
            partnerInfo.put("errorMsg", MsgUtil.getMsg(errorCode));
        }
        return partnerInfo;
    }

    @Override
    public Map<String, Object> bindingMobile(String mobile, String activeCode, String pwd) {
        Assert.hasLength(mobile, "app.bindingMobile.mobile.isnull");
        Assert.hasLength(activeCode, "app.bindingMobile.activeCode.isnull");
        Map<String, Object> result = ServiceLocator.getInstance().lookup(CiaServiceItf.class)
                .activeBindingEmailOrMobile(EnterpriseContext.getCurrentUser().getUserId(), activeCode, "2", "3");
        if (result.containsKey("errorCode")) {
            String errorCode = result.get("errorCode") + "";
            result.put("errorMsg", MsgUtil.getMsg(errorCode));
        } else {
            result = ServiceLocator.getInstance().lookup(CiaServiceItf.class).resetThridPlatformUserPwd(pwd, null);
            if (result.containsKey("errorCode")) {
                String errorCode = result.get("errorCode") + "";
                result.put("errorMsg", MsgUtil.getMsg(errorCode));
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> bindingMobileExists(String mobile) {
        if (!StringUtils.hasLength(mobile)) {
            throw new AppException("app.bindingMobile.mobile.isnull");
        }
        Map<String, Object> userInfo = ServiceLocator.getInstance().lookup(CiaServiceItf.class)
                .findUserByIdentify(mobile);
        if (userInfo.containsKey("errorCode")) {
            String errorCode = userInfo.get("errorCode") + "";
            userInfo.put("errorMsg", MsgUtil.getMsg(errorCode));
        }
        return userInfo;
    }

    @Override
    public Map<String, Object> getBindingMobile() {
        Map<String, Object> bindingResult = new HashMap<String, Object>();
        Map<String, Object> userInfo = ServiceLocator.getInstance().lookup(CiaServiceItf.class).getUserInfoFromCia();
        Boolean result = (Boolean) userInfo.get("result");
        bindingResult.put("result", result);
        if (result == true && !userInfo.containsKey("errorCode")) {
            bindingResult.put("result", result);
            bindingResult.put("mobileBinding", userInfo.get("mobileBinding"));
            bindingResult.put("mobile", userInfo.get("mobile"));
            bindingResult.put("email", userInfo.get("email"));
            bindingResult.put("emailBinding", userInfo.get("emailBinding"));
        } else {
            String errorCode = userInfo.get("errorCode") + "";
            bindingResult.put("errorCode", errorCode);
            bindingResult.put("errorMsg", MsgUtil.getMsg(errorCode));
        }
        return bindingResult;
    }

}
