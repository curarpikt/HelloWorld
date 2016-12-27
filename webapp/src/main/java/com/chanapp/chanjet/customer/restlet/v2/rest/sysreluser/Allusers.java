package com.chanapp.chanjet.customer.restlet.v2.rest.sysreluser;

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
		List<UserValue> users = null;
		String status =this.getParam("status");
		if("enable".equals(status)){
			users = ServiceLocator.getInstance().lookup(UserServiceItf.class).getAllEnableUse(0L);	
		}else{
			users = ServiceLocator.getInstance().lookup(UserServiceItf.class).getAllEnableUse(1L);	
		}
	
		if(users!=null&&users.size()>0){
			for(UserValue user:users){
				Row userRow = BoRowConvertUtil.userValue2Row(user);
				userRow.put("phone", user.getMobile());
				userRow.put("role", user.getUserRole());
				userSet.add(userRow);
			}
			userSet.setTotal(users.size());
		}
		return userSet;
	}

}
