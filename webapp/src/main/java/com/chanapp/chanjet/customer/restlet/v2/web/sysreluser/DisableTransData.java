package com.chanapp.chanjet.customer.restlet.v2.web.sysreluser;

import java.util.HashMap;
import java.util.Map;

import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;

public class DisableTransData extends BaseRestlet{

	@Override
	public Object run() {
		Long userId = this.getParamAsLong("userId");
		Long transId = this.getParamAsLong("transId");
		PrivilegeServiceItf privilegeService = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class);

		Assert.notNull(userId, "app.privilege.user.tran.paraerror");
		Assert.notNull(transId, "app.privilege.user.tran.paraerror");
		Map<String, Object> para = new HashMap<String, Object>();
		para.put(SC.owner, userId);
		PrivilegeServiceItf service = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class);
		service.transCustomer(userId, transId, para);
        BoSession session = AppWorkManager.getBoDataAccessManager().getBoSession();
		return privilegeService.diableTransData(userId, transId,session);
	}

}
