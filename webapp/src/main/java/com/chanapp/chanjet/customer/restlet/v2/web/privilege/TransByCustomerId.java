package com.chanapp.chanjet.customer.restlet.v2.web.privilege;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

public class TransByCustomerId extends BaseRestlet{
	@Override
	public Object run() {
		Long userId = this.getParamAsLong("tranfromOwner");
		Long transId = this.getParamAsLong("trantoOwner");
		String customerIds =  this.getParam("customerIds");
		Assert.notNull(userId, "app.privilege.user.tran.paraerror");
		Assert.notNull(transId, "app.privilege.user.tran.paraerror");
		Assert.notNull(customerIds, "app.privilege.user.tran.paraerror");
		String[] ids =  customerIds.split(",");
		List<Long> idlist=new ArrayList<Long>();
		for(String id:ids){
			idlist.add(Long.parseLong(id));
		}
		Map<String, Object> para = new HashMap<String, Object>();
		para.put(SC.id, idlist);
		PrivilegeServiceItf service = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class);
		return service.transCustomer(userId, transId, para);
	}
}
