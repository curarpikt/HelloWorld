package com.chanapp.chanjet.customer.restlet.v2.rest.user;

import com.chanapp.chanjet.customer.service.binding.BindingServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 是否绑定手机
 * 
 * @author tds
 *
 */
public class GetBindingMobile extends BaseRestlet {
    @Override
    public Object run() {
        return ServiceLocator.getInstance().lookup(BindingServiceItf.class).getBindingMobile();
    }

}