package com.chanapp.chanjet.customer.restlet.test;

import com.chanapp.chanjet.customer.service.cia.CiaServiceItf;
import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.util.SyncUserUtils;

public class TestChangeBossByCIA  extends BaseRestlet {

	@Override
	public Object run() {
        Long userId = this.getParamAsLong("userId");
        ServiceLocator.getInstance().lookup(CiaServiceItf.class).addAppManager(userId);
        SyncUserUtils.syncUserFromCIA(userId, false);
        return null;
	}

}
