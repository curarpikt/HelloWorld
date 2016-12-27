package com.chanapp.chanjet.customer.restlet.v2.web.usersetting;

import com.chanapp.chanjet.customer.service.usersetting.UserSettingServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 获取用户设置
 * 
 * @author tds
 *
 */
public class GetUserSetting extends BaseRestlet {
    @Override
    public Object run() {
        return ServiceLocator.getInstance().lookup(UserSettingServiceItf.class).getUserSetting();
    }

}