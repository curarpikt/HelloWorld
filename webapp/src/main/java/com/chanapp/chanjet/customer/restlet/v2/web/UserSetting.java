package com.chanapp.chanjet.customer.restlet.v2.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.service.usersetting.UserSettingServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 新增用户设置
 * 
 * @author tds
 *
 */
public class UserSetting extends BaseRestlet {
    private static final Logger logger = LoggerFactory.getLogger(UserSetting.class);

    @Override
    public Object run() {

        String payload = this.getPayload();
        Assert.notNull(payload);

        logger.info("add/edit usersetting:{}", payload);

        if (this.getMethod() == MethodEnum.PUT) {
            return ServiceLocator.getInstance().lookup(UserSettingServiceItf.class).updateUserSetting(payload);
        }

        return ServiceLocator.getInstance().lookup(UserSettingServiceItf.class).insertUserSetting(payload);

    }

}
