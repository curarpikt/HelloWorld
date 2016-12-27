package com.chanapp.chanjet.customer.restlet.v2.rest.user;

import com.chanapp.chanjet.customer.service.binding.BindingServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 绑定手机号
 * 
 * @author tds
 *
 */
public class BindingMobile extends BaseRestlet {
    @Override
    public Object run() {
        String mobile = this.getParam("mobile");
        String activeCode = this.getParam("activeCode");
        String pwd = this.getParam("pwd");

        return ServiceLocator.getInstance().lookup(BindingServiceItf.class).bindingMobile(mobile, activeCode, pwd);
    }

}
