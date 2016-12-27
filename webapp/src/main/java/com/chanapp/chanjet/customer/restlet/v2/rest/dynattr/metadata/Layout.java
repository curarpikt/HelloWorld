package com.chanapp.chanjet.customer.restlet.v2.rest.dynattr.metadata;

import java.util.Map;

import com.chanapp.chanjet.customer.constant.metadata.ContactMetaData;
import com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData;
import com.chanapp.chanjet.customer.layout.LayoutManager;
import com.chanapp.chanjet.customer.service.layout.LayoutServiceItf;
import com.chanapp.chanjet.customer.service.metadata.MetaDataServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

public class Layout extends BaseRestlet{

	@Override
	public Object run() {
/*		LayoutServiceItf layoutService = ServiceLocator.getInstance().lookup(LayoutServiceItf.class);	
		//初始化客户布局
		layoutService.initLayout(CustomerMetaData.EOName);
		//初始化联系人布局
		layoutService.initLayout(ContactMetaData.EOName);*/
//		LayoutManager.initLayout();
		MetaDataServiceItf metaDataService = ServiceLocator.getInstance().lookup(MetaDataServiceItf.class);
        Map<String, String[]> paraMap = this.getQueryParameters();
        String[] version = paraMap.get("version");
        if(version==null){
        	return metaDataService.getMetaData(null);
        }else{
    		return metaDataService.getMetaData(Long.parseLong(version[0]));
        }
	}

}
