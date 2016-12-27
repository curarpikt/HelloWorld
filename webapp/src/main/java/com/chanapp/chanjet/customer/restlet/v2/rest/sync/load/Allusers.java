package com.chanapp.chanjet.customer.restlet.v2.rest.sync.load;

import java.util.List;


import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.util.BoRowConvertUtil;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.customer.vo.RowSet;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;


public class Allusers extends BaseRestlet{

	@Override
	public Object run() {
		RowSet userSet = new RowSet();
		List<UserValue> users = ServiceLocator.getInstance().lookup(UserServiceItf.class).getAllEnableUse(0L);
		if(users!=null&&users.size()>0){
			for(UserValue user:users){
				Row userRow = BoRowConvertUtil.userValue2Row(user);
				userSet.add(userRow);
			}
			userSet.setTotal(users.size());
		}
		return userSet;
	}

}
