package com.chanapp.chanjet.customer.service.recycle.customer;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chanapp.chanjet.customer.service.recycle.BatchRecyclableObject;
import com.chanapp.chanjet.customer.service.recycle.BoReference;
import com.chanapp.chanjet.customer.service.recycle.RecyclableObjectFormatter;
import com.chanjet.csp.common.base.json.JSONArray;
import com.chanjet.csp.common.base.usertype.MobilePhone;

public class CustomizeRecyclableObjectFormatter implements RecyclableObjectFormatter {
	private static CustomizeRecyclableObjectFormatter instance = new CustomizeRecyclableObjectFormatter();
	public static RecyclableObjectFormatter getInstance() {
		return instance;
	}
	
	private CustomizeRecyclableObjectFormatter() {		
	}
	
	@Override
	public JSONObject format(List<BatchRecyclableObject> recyclables, Integer count) {
		if(count==null)
			count = 0;
		if (recyclables == null || recyclables.isEmpty()) {
			JSONObject result = new JSONObject();
			result.put("total", 0);
			result.put("items", null);
			return result;
		}		
		
		JSONArray items = new JSONArray();
		for (BatchRecyclableObject recyclable : recyclables) {
			items.put(doFormat(recyclable));
		}
		
		JSONObject result = new JSONObject();
		result.put("total", count);
		result.put("items", items);		
		return result;
	
	}
	
	@Override
	public JSONObject format(List<BatchRecyclableObject> recyclables) {
		return format(recyclables,null);
	}
	
	private JSONObject doFormat(BatchRecyclableObject recyclable) {		
		JSONObject formatted = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("id", recyclable.getRecyclableId());
		data.put("entityName", recyclable.getBoName());
		data.put("operTime", recyclable.getOperationTime());
		data.put("operUser", recyclable.getOperationUser());
		data.put("reason", recyclable.getReason());
		
		boolean isCustomerBo = "Customer".equals(recyclable.getBoName()) ? true : false;
		boolean isContactBo = "Contact".equals(recyclable.getBoName()) ? true : false;
		boolean isWorkRecordBo = "WorkRecord".equals(recyclable.getBoName()) ? true : false;
		JSONArray relations = new JSONArray();
		if (isCustomerBo) {			
			Map<String, List<BoReference>> sourceReferenceMap = recyclable.getSourceReferenceMap();
			for (String customerPk : sourceReferenceMap.keySet()) {
				List<BoReference> sourceReferences = sourceReferenceMap.get(customerPk);
				int contactNumber = getSourceReferenceNumberOf("Contact", sourceReferences);
				int workRecordNumber = getSourceReferenceNumberOf("WorkRecord", sourceReferences);
				
				JSONObject content = new JSONObject();
				content.put("contactNum", contactNumber);
				content.put("workRecordNum", workRecordNumber);
				
				JSONObject relation = new JSONObject();
				relation.put("content", JSON.toJSONString(content));
				relation.put("customerName", recyclable.getNameMap().get(customerPk));
				relation.put("relationId", recyclable.getRecyclableId());
				relation.put("entityName", recyclable.getBoName());
				relation.put("entityId", customerPk);
				relations.put(relation);
			}			
		} else if (isContactBo) {			
			JSONObject relation = new JSONObject();
			String customerName = (String) recyclable.getFieldValueOf("customerCopy");
			String objectName = (String) recyclable.getNameMap().values().toArray()[0];
			relation.put("content", null);
			relation.put("contactName", objectName);
			relation.put("refCustomerName", customerName);
		
			String mobilePhone = (String) recyclable.getFieldValueOf("mobile");
/*            if (mobilePhone != null) {
                mobile = mobilePhone.getPhoneNumber();
            }*/
			relation.put("mobile", mobilePhone);
			relation.put("relationId", recyclable.getRecyclableId());
			relation.put("entityName", recyclable.getBoName());
			
			relations.put(relation);
		} else if (isWorkRecordBo) {			
			JSONObject relation = new JSONObject();
			String customerName = (String) recyclable.getFieldValueOf("customerCopy");
			String objectName = (String) recyclable.getFieldValueOf("content");
			relation.put("content", null);
			relation.put("workContent", objectName);
			relation.put("refCustomerName", customerName);			
			relation.put("relationId", recyclable.getRecyclableId());
			relation.put("entityName", recyclable.getBoName());
			
			relations.put(relation);
		} 
		
		formatted.put("recycle", data);
		formatted.put("recycleRelations", relations);
		formatted.put("operSize", recyclable.getObjectSize());
		return formatted;
	}

	private int getSourceReferenceNumberOf(String sourceBo, List<BoReference> sourceReferences) {
		if (sourceReferences == null || sourceReferences.isEmpty()) {
			return 0;
		}
		
		int number = 0;
		for (BoReference reference : sourceReferences) {
			if (sourceBo.equals(reference.getEntityRelationship().getSourceBo())) {
				number = reference.getSubjectRelationships().size();
			}
		}
		return number;
	}


}
