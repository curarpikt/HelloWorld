package com.chanapp.chanjet.customer.restlet.v2.web.invite;

import java.util.HashMap;
import java.util.Map;

import com.chanapp.chanjet.customer.service.cia.CiaServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.common.base.util.Assert;
import com.chanjet.csp.common.base.util.StringUtils;

/**
 * 修改当前用户企业名称
 * 
 * @author tds
 *
 */
public class UpdateOrgName extends BaseRestlet {
    @Override
    public Object run() {
        String payload = this.getPayload();
        Assert.notNull(payload);

        Map<String, Object> map = dataManager.jsonStringToMap(payload);

        String orgName = (String) map.get("orgName");

        if (StringUtils.isEmpty(orgName)) {
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("result", false);
            result.put("errorCode", "10702");
            result.put("param", "企业名称为空");
            return result;
        }

        return ServiceLocator.getInstance().lookup(CiaServiceItf.class)
                .updateOrganizationName(EnterpriseContext.getCurrentUser().getOrgId(), orgName);

    }

}
