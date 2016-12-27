package com.chanapp.chanjet.customer.restlet.v2.rest.user;

import com.chanapp.chanjet.customer.service.binding.BindingServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 发送绑定用户账号的激活邮件或手机短信
 * 
 * @author tds
 *
 */
public class SendBindingMobileMsg extends BaseRestlet {
    @Override
    public Object run() {
        String mobile = this.getParam("mobile");

        return ServiceLocator.getInstance().lookup(BindingServiceItf.class).sendBindingMobileMsg(mobile);
    }

}
