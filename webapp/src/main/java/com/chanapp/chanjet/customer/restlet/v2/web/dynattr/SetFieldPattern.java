package com.chanapp.chanjet.customer.restlet.v2.web.dynattr;

import java.util.Map;

import com.chanapp.chanjet.customer.service.metadata.MetaDataServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

public class SetFieldPattern extends BaseRestlet{

	@SuppressWarnings("unchecked")
    @Override
	public Object run() {
		String payload = this.getPayload();
		Map<String,Object> para = dataManager.jsonStringToMap(payload);
		String entityTable = (String)para.get("entityName"); 
		String fieldName = (String)para.get("fieldName"); 
		String patternStr = (String)para.get("pattern");
		Map<String, Object> pattern = dataManager.jsonStringToMap(patternStr); 	
		MetaDataServiceItf metaDataService = ServiceLocator.getInstance().lookup(MetaDataServiceItf.class);
		return metaDataService.setFieldPattern(entityTable, fieldName,pattern);
	}

}
