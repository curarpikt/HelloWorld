package com.chanapp.chanjet.customer.restlet.v2.web.usersetting;

import com.chanapp.chanjet.customer.service.usersetting.UserSettingServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 端获取企业上下班时间设置/设置企业上下班时间
 * 
 * @author tds
 *
 */
public class WorkingHours extends BaseRestlet {
    @Override
    public Object run() {
        if (this.getMethod() == MethodEnum.GET) {
            return ServiceLocator.getInstance().lookup(UserSettingServiceItf.class).workingHours();
        } else {
            return ServiceLocator.getInstance().lookup(UserSettingServiceItf.class).setWorkingHours(this.getPayload());
        }
    }

}
