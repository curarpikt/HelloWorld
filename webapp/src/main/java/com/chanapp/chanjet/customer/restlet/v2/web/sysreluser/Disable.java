package com.chanapp.chanjet.customer.restlet.v2.web.sysreluser;

import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;

public class Disable extends BaseRestlet{

	@Override
	public Object run() {
        Long userId = this.getParamAsLong("userId");
        BoSession session = AppWorkManager.getBoDataAccessManager().getBoSession();
        return ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class).disableUser(userId,session);
	}

}
