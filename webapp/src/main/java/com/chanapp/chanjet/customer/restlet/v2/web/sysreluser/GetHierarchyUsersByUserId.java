package com.chanapp.chanjet.customer.restlet.v2.web.sysreluser;

import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

public class GetHierarchyUsersByUserId extends BaseRestlet{

	@Override
	public Object run() {
        UserServiceItf userService = ServiceLocator.getInstance().lookup(UserServiceItf.class);
        return userService.getHierarchyUsers(this.getParamAsLong("userId"));
	}

}
