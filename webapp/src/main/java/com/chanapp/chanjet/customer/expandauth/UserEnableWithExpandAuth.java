package com.chanapp.chanjet.customer.expandauth;

import com.chanapp.chanjet.web.context.AppContext;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.enterprise.ext.ExpandAuthorization;

public class UserEnableWithExpandAuth  implements ExpandAuthorization{
	@Override
	public void process(Object[] args) {
		Long userId = null;
		if (args != null) {
			userId = (Long) args[0]; // 预定传入参数为RoleId
		}
		BoSession session = AppContext.session();
		AppWorkManager.getBoDataAccessManager().getDataAuthManagement().setUserEnable(userId, true, session);
	}

}
