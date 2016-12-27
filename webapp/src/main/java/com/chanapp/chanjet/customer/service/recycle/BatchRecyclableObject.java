package com.chanapp.chanjet.customer.service.recycle;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.chanjet.csp.appmanager.AppWorkManager;

public class BatchRecyclableObject {	
	private Long recyclableId;
	private int objectSize;
	private String boName;
	private String reason;
	private Map<String, String> operationUser;


	private Timestamp operationTime;
	
	// keyed by objectPK
	private Map<String, String> jsonDataMap;
	private Map<String, String> nameMap;
	private Map<String, List<BoReference>> sourceReferenceMap = new HashMap<>();
	private Map<String, List<BoReference>> targetReferenceMap = new HashMap<>();
	
	public BatchRecyclableObject(String boName, Long recyclableId, 
			Map<String, String> jsonDataMap, Map<String, String> nameMap, 
			String reason, Map<String, String> operUser, Timestamp operationTime,
			Map<String, List<BoReference>> sourceReferenceMap, 
			Map<String, List<BoReference>> targetReferenceMap) {
		this.boName = boName;
		this.recyclableId = recyclableId;
		this.jsonDataMap = jsonDataMap;
		this.nameMap = nameMap;
		this.reason = reason;
		this.operationUser = operUser;
		this.operationTime = operationTime;
		this.sourceReferenceMap = sourceReferenceMap;
		this.targetReferenceMap = targetReferenceMap;
		this.objectSize = jsonDataMap.keySet().size();
	}

	public BatchRecyclableObject(String boName, Long recyclableId, Map<String, String> jsonDataMap,
			Map<String, String> nameMap, Map<String, List<BoReference>> sourceReferenceMap,
			Map<String, List<BoReference>> targetReferenceMap) {
		this.boName = boName;
		this.recyclableId = recyclableId;
		this.jsonDataMap = jsonDataMap;
		this.nameMap = nameMap;
		this.sourceReferenceMap = sourceReferenceMap;
		this.targetReferenceMap = targetReferenceMap;
	}

	public Long getRecyclableId() {
		return recyclableId;
	}


	public void setRecyclableId(Long recyclableId) {
		this.recyclableId = recyclableId;
	}


	public String getBoName() {
		return boName;
	}


	public void setBoName(String boName) {
		this.boName = boName;
	}


	public Map<String, List<BoReference>> getSourceReferenceMap() {
		return sourceReferenceMap;
	}


	public void setSourceReferenceMap(Map<String, List<BoReference>> sourceReferenceMap) {
		this.sourceReferenceMap = sourceReferenceMap;
	}


	public Map<String, List<BoReference>> getTargetReferenceMap() {
		return targetReferenceMap;
	}


	public void setTargetReferenceMap(Map<String, List<BoReference>> targetReferenceMap) {
		this.targetReferenceMap = targetReferenceMap;
	}
	
	public String getReason() {
		return this.reason;
	}





	public Timestamp getOperationTime() {
		return operationTime;
	}


	public void setOperationTime(Timestamp operationTime) {
		this.operationTime = operationTime;
	}


	public int getObjectSize() {
		return objectSize;
	}


	public void setObjectSize(int objectSize) {
		this.objectSize = objectSize;
	}
	
	public Object getFieldValueOf(String objectPk, String fieldName) {
		String jsonDataString = jsonDataMap.get(objectPk);
		LinkedHashMap<String, Object> jsonData = 
				AppWorkManager.getDataManager().fromJSONString(jsonDataString, LinkedHashMap.class);
		return jsonData.get(fieldName);
	}
	
	public Object getFieldValueOf(String fieldName) {
		String jsonDataString = (String) jsonDataMap.values().toArray()[0];
		LinkedHashMap<String, Object> jsonData = 
				AppWorkManager.getDataManager().fromJSONString(jsonDataString, LinkedHashMap.class);
		return jsonData.get(fieldName);
	}

	public Map<String, String> getNameMap() {
		return nameMap;
	}

	public void setNameMap(Map<String, String> nameMap) {
		this.nameMap = nameMap;
	}
	
	public Map<String, String> getOperationUser() {
		return operationUser;
	}

	public void setOperationUser(Map<String, String> operationUser) {
		this.operationUser = operationUser;
	}
}
