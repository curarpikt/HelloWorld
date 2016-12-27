package com.chanapp.chanjet.customer.restlet.mobile.checkin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;

public class NearbyCustomers  extends BaseRestlet{

	@Override
	public Object run() {
		if (this.getMethod().equals(MethodEnum.GET)) {
			Assert.notNull(this.getParamAsInt("first"), "app.common.para.format.error");
			Assert.notNull(this.getParamAsInt("max"), "app.common.para.format.error");
			int first = this.getParamAsInt("first");
			int pageSize = this.getParamAsInt("max");
			Double latitude=this.getParamAsDouble("latitude");
			Double longitude=this.getParamAsDouble("longitude");		
            return getNearbyCustomers(latitude,longitude,first,pageSize);
	}
		return null;
	}
	
	public String getNearbyCustomers(Double latitude,Double longitude,Integer first,Integer max){
		String retMap = ServiceLocator.getInstance().lookup(CustomerServiceItf.class).getCheckInCustomer(latitude,longitude,first,max);	
		return retMap;
	}

}
