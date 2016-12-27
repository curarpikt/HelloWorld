package com.chanapp.chanjet.customer.restlet.v2.rest.user;

import com.chanapp.chanjet.customer.service.binding.BindingServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 查询服务商信息
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