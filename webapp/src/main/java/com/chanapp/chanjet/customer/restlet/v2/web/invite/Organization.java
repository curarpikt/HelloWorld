package com.chanapp.chanjet.customer.restlet.v2.web.invite;

import java.util.HashMap;
import java.util.Map;

import com.chanapp.chanjet.customer.service.cia.CiaServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;

/**
 * 查询用户当前企业名称
 * 
 * @author tds
 *
 */
public class Organization extends BaseRestlet {
    @Override
    public Object run() {
        Map<String, Object> ciaOrgMap = ServiceLocator.getInstance().lookup(CiaServiceItf.class)
                .getOrganizationInfoByOrgId(EnterpriseContext.getCurrentUser().getOrgId());
        // 企业信息未取到
        if (ciaOrgMap == null) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("result", false);
            map.put("errorCode", "20204");
            map.put("param", "组织不存在");
            return map;
        }

        if (ciaOrgMap.containsKey("errorCode")) {
            return ciaOrgMap;
            // 企业信息获取成功，从中取到企业名称返回。
        } else {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("result", true);
            map.put("orgName", ciaOrgMap.get("orgFullName"));
            return map;
        }

    }

}
