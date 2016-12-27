package com.chanapp.chanjet.customer.restlet.v2.web.sysreluser;

import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.customer.vo.VORowSet;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

public class Alltranusers extends BaseRestlet{

	@Override
	public Object run() {
        Map<String, String[]> paraMap = this.getQueryParameters();
        String[] userId = paraMap.get("userId");
	    UserServiceItf userService = ServiceLocator.getInstance().lookup(UserServiceItf.class);	
        Assert.notNull(userId);
        VORowSet<UserValue> uses =new VORowSet<UserValue>();
        List<UserValue> uservalues = userService.getAllEnableUse(Long.parseLong(userId[0]));
        uses.setItems(uservalues);
        uses.setTotal(uservalues.size());
        return uses;
	}

}
