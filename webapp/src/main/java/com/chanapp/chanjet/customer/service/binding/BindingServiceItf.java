package com.chanapp.chanjet.customer.service.binding;

import java.util.Map;

import com.chanapp.chanjet.web.service.BaseServiceItf;

public interface BindingServiceItf extends BaseServiceItf {
    Map<String, Object> sendBindingMobileMsg(String mobile);

    Map<String, Object> getOrgBindingPartner(String orgId);

    Map<String, Object> getPartnerInfoById(String partnerId);

    Map<String, Object> bindingOrgPartner(String orgId, String partnerId);

    Map<String, Object> bindingMobile(String mobile, String activeCode, String pwd);

    Map<String, Object> bindingMobileExists(String mobile);

    Map<String, Object> getBindingMobile();
}
