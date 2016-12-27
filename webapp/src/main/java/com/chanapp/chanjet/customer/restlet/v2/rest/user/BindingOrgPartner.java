package com.chanapp.chanjet.customer.restlet.v2.rest.user;

import com.chanapp.chanjet.customer.service.binding.BindingServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 绑定服务商
 * 
 * @author tds
 *
 */
public class BindingOrgPartner extends BaseRestlet {
    @Override
    public Object run() {
        String orgId = this.getParam("orgId");
        String partnerId = this.getParam("partnerId");

        return ServiceLocator.getInstance().lookup(BindingServiceItf.class).bindingOrgPartner(orgId, partnerId);
    }

}
