package com.chanapp.chanjet.customer.restlet.gzq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.chanapp.chanjet.customer.businessobject.api.metadata.constants.BONames;
import com.chanapp.chanjet.customer.service.importrecord.ImportContactImpl;
import com.chanapp.chanjet.customer.service.importrecord.ImportCustomerImpl;
import com.chanapp.chanjet.customer.service.importrecord.ImportManger;
import com.chanapp.chanjet.customer.service.importrecord.ImportWorkRecordImpl;
import com.chanapp.chanjet.customer.service.importrecordnew.ImportRecordNewServiceItf;
import com.chanapp.chanjet.web.context.AppContext;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

public class ImportData  extends BaseRestlet{

	@Override
	public Object run() {
		String payload =this.getPayload();
        Map<String, Object> importMap = dataManager.jsonStringToMap(payload);
		ImportRecordNewServiceItf service = ServiceLocator.getInstance().lookup(ImportRecordNewServiceItf.class);	
	
		Map<String, List<Object>> rsMap = (Map<String, List<Object>>)importMap.get("data");
		Map <String, List<String>> transMap = new HashMap<String, List<String>>();
		for(Entry<String, List<Object>> entry:rsMap.entrySet()){
			List<String> tempList = transList(entry.getValue());	
			transMap.put(entry.getKey(), tempList);
		}
		List<String> headers1 = (List<String>)importMap.get("customerHeader");
		List<String> headers2 = (List<String>)importMap.get("contactHeader");
		List<String> headers3 = (List<String>)importMap.get("workrecordHeader");
		Long id =  service.importRecordFromGZQ(headers1,headers2,headers3);
		ImportManger manager = new ImportManger(id, transMap);	
		manager.registerImportService(BONames.Customer, new ImportCustomerImpl());
		manager.registerImportService(BONames.Contact, new ImportContactImpl());
		manager.registerImportService(BONames.WorkRecord, new ImportWorkRecordImpl());
		Map<String, Object> result =manager.importTask(AppContext.getWebContextSession());
		//Map<String, Object> result = service.task(id, transMap);
		return result;
	}
	
	private List<String> transList(List<Object> target){
		List<String> retList = new ArrayList<String>();
		for(Object value : target){
			if(value instanceof LinkedHashMap){
				retList.add(JSON.toJSONString(value));
			}else if(value instanceof String){
				retList.add((String)value);
			}
		
		}
		return retList;
	}

}
