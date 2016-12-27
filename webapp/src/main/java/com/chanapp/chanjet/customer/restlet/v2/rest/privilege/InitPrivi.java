package com.chanapp.chanjet.customer.restlet.v2.rest.privilege;

import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;

/**
 * 是否应用管理员
 * 
 * @author tds
 *
 */
public class InitPrivi extends BaseRestlet {
    @Override
    public Object run() {
        Row ret = new Row();
        boolean isBoss = ServiceLocator.getInstance().lookup(UserServiceItf.class)
                .isOrgBoss(EnterpriseContext.getCurrentUser());
        ret.put("superUser", isBoss);
        return ret;
    }

}
