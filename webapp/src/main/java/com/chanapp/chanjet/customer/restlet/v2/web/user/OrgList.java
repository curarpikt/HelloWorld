package com.chanapp.chanjet.customer.restlet.v2.web.user;

import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 我加入的企业列表
 * 
 * @author tds
 *
 */
public class OrgList extends BaseRestlet {
    @Override
    public Object run() {
        return ServiceLocator.getInstance().lookup(UserServiceItf.class).belongOrganization();
    }

}
