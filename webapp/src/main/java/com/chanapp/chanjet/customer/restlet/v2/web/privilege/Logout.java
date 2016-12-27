package com.chanapp.chanjet.customer.restlet.v2.web.privilege;

import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

public class Logout  extends BaseRestlet{

	@Override
	public Object run() {
        UserServiceItf userService = ServiceLocator.getInstance().lookup(UserServiceItf.class);
		 userService.logOut();
		 return "";
	}

}
