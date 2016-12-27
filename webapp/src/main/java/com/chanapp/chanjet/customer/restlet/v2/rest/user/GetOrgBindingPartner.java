package com.chanapp.chanjet.customer.restlet.v2.rest.user;

import com.chanapp.chanjet.customer.service.binding.BindingServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 获取企业绑定服务商
 * 
 * @author tds
 *
 */
public class GetOrgBindingPartner extends BaseRestlet {
    @Override
    public Object run() {
        String orgId = this.getParam("orgId");

        return ServiceLocator.getInstance().lookup(BindingServiceItf.class).getOrgBindingPartner(orgId);
    }

}
