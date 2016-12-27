package com.chanapp.chanjet.customer.restlet.v2.rest.sysreluser;

import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;

/**
 * 停用用户，取消数据权限。删除上下级关系，变更应用用户状态
 * 
 * @author tds
 *
 */
public class Disable extends BaseRestlet {
    @Override
    public Object run() {
        Long userId = this.getParamAsLong("userId");
        BoSession session = AppWorkManager.getBoDataAccessManager().getBoSession();
        return ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class).disableUser(userId,session);
    }

}
