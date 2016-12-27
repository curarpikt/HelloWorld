package com.chanapp.chanjet.customer.service.recycle.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.service.recycle.BoReference;

public class DatabaseBatchObjectReference {
	private String theBoName;
	private List<Long> objectIds = new ArrayList<>();
	private Map<String, String> pkMap = new HashMap<>();
	private Map<String, String> jsonDataMap = new HashMap<>();
	private Map<String, List<BoReference>> sourceReferenceMap = new HashMap<>();
	private Map<String, List<BoReference>> targetReferenceMap = new HashMap<>();
	private Map<Long, DatabaseObjectReference> dbReferences = new HashMap<>();
	private Map<String, String> nameMap = new HashMap<>();
	
	public DatabaseBatchObjectReference(String boName, List<Long> objectIds) {
		this.theBoName = boName;
		this.objectIds = objectIds;
		
		loadDataAndReferences();
	}	

	private void loadDataAndReferences() {
		if (objectIds == null || objectIds.isEmpty()) {
			return;
		}
		
		for (Long objectId : objectIds) {
			DatabaseObjectReference dbReference = new DatabaseObjectReferenceImpl(theBoName, objectId);
			dbReferences.put(objectId, dbReference);
			String objectPk = dbReference.getPk();
			String objectName = dbReference.getName();
			
			jsonDataMap.put(objectPk, dbReference.getJsonData());
			nameMap.put(objectPk, objectName);
			sourceReferenceMap.put(objectPk, dbReference.getSourceReferences());
			targetReferenceMap.put(objectPk, dbReference.getTargetReferences());
		}
	}
	
	public void delete() {
		if (objectIds == null || objectIds.isEmpty()) {
			return;
		}
		
		for (Long objectId : objectIds) {
			DatabaseObjectReference dbReference = dbReferences.get(objectId);
			dbReference.delete();
		}
	}
	
	public Map<String, String> getJsonDataMap() {
		return this.jsonDataMap;
	}

	public Map<String, List<BoReference>> getSourceReferenceMap() {		
		return this.sourceReferenceMap;
	}
	
	public Map<String, List<BoReference>> getTargetReferenceMap() {		
		return this.targetReferenceMap;
	}	

	public Map<String, String> getNameMap() {		
		return this.nameMap;
	}
	
	/************************************ 还原 ***************************************/
	public DatabaseBatchObjectReference(String boName, Map<String, String> jsonDataMap,
			Map<String, List<BoReference>> sourceReferenceMap, Map<String, List<BoReference>> targetReferenceMap) {
		this.theBoName = boName;
		this.jsonDataMap = jsonDataMap;
		this.sourceReferenceMap = sourceReferenceMap;
		this.targetReferenceMap = targetReferenceMap;
	}

	public List<Long> save() {
		List<Long> recycledObjectIds = new ArrayList<>();
		for (String objectPk : jsonDataMap.keySet()) {
			String jsonData = jsonDataMap.get(objectPk);					
			List<BoReference> sourceReferences = sourceReferenceMap.get(objectPk);
			List<BoReference> targetReferences = targetReferenceMap.get(objectPk);
			DatabaseObjectReference dbDataReference = 
					new DatabaseObjectReferenceImpl(theBoName, jsonData, sourceReferences, targetReferences);
			Long recycledObjectId = dbDataReference.save();
			recycledObjectIds.add(recycledObjectId);
			String recycledObjectPk = dbDataReference.getPk();
			this.pkMap.put(objectPk, recycledObjectPk);
		}
			
		return recycledObjectIds;
	}

	public Map<String, String> getPkMap() {		
		return this.pkMap;
	}

}
