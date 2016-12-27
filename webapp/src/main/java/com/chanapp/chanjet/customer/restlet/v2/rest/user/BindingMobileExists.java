package com.chanapp.chanjet.customer.restlet.v2.rest.user;

import com.chanapp.chanjet.customer.service.binding.BindingServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 绑定手机号是否已存在
 * 
 * @author tds
 *
 */
public class BindingMobileExists extends BaseRestlet {
    @Override
    public Object run() {
        String mobile = this.getParam("mobile");

        return ServiceLocator.getInstance().lookup(BindingServiceItf.class).bindingMobileExists(mobile);
    }

}
