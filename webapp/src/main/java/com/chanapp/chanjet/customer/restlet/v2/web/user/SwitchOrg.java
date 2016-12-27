package com.chanapp.chanjet.customer.restlet.v2.web.user;

import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 切换用户默认企业
 * 
 * @author tds
 *
 */
public class SwitchOrg extends BaseRestlet {
    @Override
    public Object run() {
        Long orgId = this.getId();

        return ServiceLocator.getInstance().lookup(UserServiceItf.class).switchOrganization(orgId);
    }

}
