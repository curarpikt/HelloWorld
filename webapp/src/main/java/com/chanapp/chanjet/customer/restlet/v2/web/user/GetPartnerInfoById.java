package com.chanapp.chanjet.customer.restlet.v2.web.user;

import com.chanapp.chanjet.customer.service.binding.BindingServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 按ID查询绑定服务商
 * 
 * @author tds
 *
 */
public class GetPartnerInfoById extends BaseRestlet {
    @Override
    public Object run() {
        String partnerId = this.getParam("partnerId");

        return ServiceLocator.getInstance().lookup(BindingServiceItf.class).getPartnerInfoById(partnerId);
    }

}
