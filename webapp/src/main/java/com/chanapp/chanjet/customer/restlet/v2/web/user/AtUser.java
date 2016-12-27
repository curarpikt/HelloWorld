package com.chanapp.chanjet.customer.restlet.v2.web.user;

import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 获取@用户列表
 * 
 * @author tds
 *
 */
public class AtUser extends BaseRestlet {
    @Override
    public Object run() {
        String keyWord = this.getParam("keyWord");

        return ServiceLocator.getInstance().lookup(UserServiceItf.class).getAtList(keyWord);
    }

}
