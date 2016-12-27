package com.chanapp.chanjet.customer.restlet.v2.web.recycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.recyclableobject.IRecyclableObjectRow;
import com.chanapp.chanjet.customer.businessobject.api.recyclablerelation.IRecyclableRelationRow;
import com.chanapp.chanjet.customer.service.recycle.RecyclableBinManager;
import com.chanapp.chanjet.customer.service.recycle.RecyclableObjectService;
import com.chanapp.chanjet.customer.service.recycle.RecyclableObjectServiceImpl;
import com.chanapp.chanjet.customer.service.recycle.RecyclableRelationService;
import com.chanapp.chanjet.customer.service.recycle.RecyclableRelationServiceImpl;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.vo.AppextResult;
import com.chanapp.chanjet.web.restlet.BaseRestlet;

public class RecyclableBinController extends BaseRestlet {

	@Override
	public Object run() {
		if (this.getMethod().equals(MethodEnum.GET)) {
			return doGet();
		} else if (this.getMethod().equals(MethodEnum.POST)) {			
			return doPost();
		}else if(this.getMethod().equals(MethodEnum.PUT)){
			return doPut();
		} else if(this.getMethod().equals(MethodEnum.DELETE)){
			return doDelete();
		}
		
		return null;		
	}

	private Object doPost() {		
		String payload = this.getPayload();
        Assert.notNull(payload);
        LinkedHashMap<String, Object> param = (LinkedHashMap<String, Object>) dataManager.jsonStringToMap(payload);
        String boName = param.get("boName") == null ? null : param.get("boName").toString();
        
        Map<String, Object> result;
        boolean batch = (boolean) param.get("batch");
        if (batch) {
        	List<Long> objectIds = new ArrayList<Long>();
        	List<Integer> ids = (List<Integer>) param.get("objectIds");
        	for (Integer id : ids) {
        		objectIds.add(Long.valueOf(id.toString()));
        	}
        	
        	String reason = (String) param.get("reason");
        	result = RecyclableBinManager.putBatch(boName, objectIds, reason);        	
        } else {
        	Long objectId = Long.valueOf(param.get("objectId").toString());        	   
        	result = RecyclableBinManager.put(boName, objectId);
        }
       
		return result;
	}
	
	private Object doGet() {		
        Long recyclableId = this.getParamAsLong("recyclableId");        
        IRecyclableObjectRow recyclable = null;
        if (recyclableId != null) {
        	 RecyclableObjectService objectService = new RecyclableObjectServiceImpl();
             recyclable = objectService.get(recyclableId);	
        }
       
        Long relationId = this.getParamAsLong("relationId");
        IRecyclableRelationRow relation = null;
        if (relationId != null) {
        	 RecyclableRelationService relationService = new RecyclableRelationServiceImpl();
             relation = relationService.get(relationId);	
        }
		Map<String, Object> result = new HashMap<>();
		result.put("recyclable", recyclable);
		result.put("relation", relation);
		return result;
	}
	
	private Object doPut() {
		String payload = this.getPayload();
        Assert.notNull(payload);
        LinkedHashMap<String, Object> param = (LinkedHashMap<String, Object>) dataManager.jsonStringToMap(payload);      
        Long recyclableId = param.get("recyclableId") == null ? null : Long.valueOf(param.get("recyclableId").toString());
		List<Long> newObjectId = RecyclableBinManager.recycle(recyclableId);
		return new AppextResult(newObjectId);
	}	

	private Object doDelete() {
		/*String payload = this.getPayload();
        Assert.notNull(payload);
        LinkedHashMap<String, Object> param = (LinkedHashMap<String, Object>) dataManager.jsonStringToMap(payload);*/
        String[] ids = ((String) getParam("recyclableIds", 0)).split(",");
        List<Long> toDeletedIds = new ArrayList<>();
        for (String id : ids) {
        	toDeletedIds.add(Long.valueOf(id));
        }
        return new AppextResult(RecyclableBinManager.delete(toDeletedIds));
	}

}
