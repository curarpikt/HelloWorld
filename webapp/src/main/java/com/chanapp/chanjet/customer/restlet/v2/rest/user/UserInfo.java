package com.chanapp.chanjet.customer.restlet.v2.rest.user;

import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanapp.chanjet.web.util.ConvertUtil;

/**
 * 修改用户头像和名称
 * 
 * @author tds
 *
 */
public class UserInfo extends BaseRestlet {
    @Override
    public Object run() {
        String name = this.getParam("name");
        String headPic = this.getParam("headPic");

        return ServiceLocator.getInstance().lookup(UserServiceItf.class).modify(name, headPic);
    }

}