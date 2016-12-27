package com.chanapp.chanjet.customer.restlet.v2.web.dynattr;

import com.chanapp.chanjet.customer.service.metadata.MetaDataServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

public class Enums extends BaseRestlet{

	@Override
	public Object run() {
		MetaDataServiceItf metaDataService = ServiceLocator.getInstance().lookup(MetaDataServiceItf.class);
		return metaDataService.findENumList();
	}

}
