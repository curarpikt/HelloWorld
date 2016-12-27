package com.chanapp.chanjet.customer.restlet.v2.web.privilege;

import java.util.HashMap;
import java.util.Map;

import com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData;
import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

public class TransByCondition extends BaseRestlet{
	@Override
	public Object run() {
		Long userId = this.getParamAsLong("tranfromOwner");
		Long transId = this.getParamAsLong("trantoOwner");
		String queryValue =  this.getParam("queryValue");
		Map<String, Object> para = new HashMap<String, Object>();
		para.put(CustomerMetaData.conditions, queryValue);
		PrivilegeServiceItf service = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class);
		return service.transCustomer(userId, transId, para);
	}
}
