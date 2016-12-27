package com.chanapp.chanjet.customer.restlet.v2.web.sysreluser;

import java.util.List;

import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.customer.vo.VORowSet;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;

public class AllSubordinate extends BaseRestlet{

	@Override
	public Object run() {		
        UserServiceItf userService = ServiceLocator.getInstance().lookup(UserServiceItf.class);
        VORowSet<UserValue> uses =new VORowSet<UserValue>();
        List<UserValue> uservalues = userService.getHierarchyUsers(EnterpriseContext.getCurrentUser().getUserLongId());
        uses.setItems(uservalues);
        uses.setTotal(uservalues.size());
		return uses;
	}

}
