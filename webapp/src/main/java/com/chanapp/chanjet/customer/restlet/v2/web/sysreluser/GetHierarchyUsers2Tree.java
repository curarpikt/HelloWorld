package com.chanapp.chanjet.customer.restlet.v2.web.sysreluser;

import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

public class GetHierarchyUsers2Tree extends BaseRestlet{

	@Override
	public Object run() {
		String monthStart = this.getParam("monthStart");
		String monthEnd = this.getParam("monthEnd");
		String bizType = this.getParam("bizType");
	    UserServiceItf userService = ServiceLocator.getInstance().lookup(UserServiceItf.class);
		return userService.getHierarchyUsers2Tree(monthStart, monthEnd, bizType);
	}

}
