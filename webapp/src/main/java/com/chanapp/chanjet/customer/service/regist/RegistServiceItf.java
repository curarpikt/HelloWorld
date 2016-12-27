package com.chanapp.chanjet.customer.service.regist;

import java.util.Map;

import com.chanapp.chanjet.web.service.BaseServiceItf;

public interface RegistServiceItf extends BaseServiceItf {
    Map<String, Object> getOrganizationInfoByOrgId(String orgId);
}
