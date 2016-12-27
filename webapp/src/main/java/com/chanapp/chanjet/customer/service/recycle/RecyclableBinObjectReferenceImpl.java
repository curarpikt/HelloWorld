package com.chanapp.chanjet.customer.service.recycle;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.recyclableobject.IRecyclableObjectRow;
import com.chanapp.chanjet.customer.businessobject.api.recyclablerelation.IRecyclableRelationRow;
import com.chanapp.chanjet.customer.businessobject.api.recyclablerelation.IRecyclableRelationRowSet;
import com.chanapp.chanjet.customer.service.recycle.db.DbHelper;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;

public class RecyclableBinObjectReferenceImpl implements RecyclableBinObjectReference {
	
	private RecyclableObjectService objectService = RecyclableObjectServiceImpl.getInstance();
	private RecyclableRelationService relationService = RecyclableRelationServiceImpl.getInstance();
	
	private String theObjectPk;
	private String theBoName;
	private String theJsonData;
	private String theObjectName;
	private IRecyclableObjectRow theRecyclable; 
	private List<BoReference> sourceReferences;
	private List<BoReference> targetReferences;
	
	private Long theRecyclableId;
	private Long userId;	
	
	public RecyclableBinObjectReferenceImpl(String boName, String objectPk, String jsonData, 
			String objectName, Long userId, List<BoReference> sourceReferences, List<BoReference> targetReferences) {
		this.theObjectPk = objectPk;
		this.theJsonData = jsonData;
		this.theBoName = boName;
		this.theObjectName = objectName;
		this.sourceReferences = sourceReferences;
		this.targetReferences = targetReferences;
		this.userId = userId;
	}

	public RecyclableBinObjectReferenceImpl(Long recyclableId) {
		this.theRecyclableId = recyclableId;
		this.theRecyclable = objectService.get(recyclableId);
		this.theJsonData = theRecyclable.getJsonData();
		this.theObjectPk = theRecyclable.getObjectPk();
		this.theBoName = theRecyclable.getBoName();
		
		loadReferences();
	}

	private void loadReferences() {	
		this.sourceReferences = loadReferences(true);
		this.targetReferences = loadReferences(false);
	}

	@Override
	public Map<String, Object> save() {
		// 保存引用关系
		List<Long> relationIds = addReferences();		
		this.theRecyclableId = saveData();	
		// 回写之前保存的引用关系
		updateObjectStatus(true);
		
		Map<String, Object> result = new HashMap<>();
		result.put("recyclableId", theRecyclableId);
		result.put("relationIds", relationIds);
		return result;
	}

	private void updateObjectStatus(boolean whenAdd) {
		relationService.updateStatusOf(theObjectPk, whenAdd);
		
	}

	private Long saveData() {		
		Timestamp operationTime = new Timestamp(new Date().getTime());
		Map<String, String> jsonDataMap = new HashMap<>();
		jsonDataMap.put(theObjectPk, theJsonData);
		String batchJsonData = DbHelper.toJsonString(jsonDataMap);
		Map<String, String> nameMap = new HashMap<>();
		nameMap.put(theObjectPk, theObjectName);
		String batchNames = DbHelper.toJsonString(nameMap);
		Long recyclableId = objectService.put(theBoName, theObjectPk, batchJsonData, batchNames, null, userId, operationTime);
		return recyclableId;
	}

	private List<Long> addReferences() {
		Map<String, List<BoReference>> references = new HashMap<>();
		references.put("source", sourceReferences);
		references.put("target", targetReferences);
		List<Long> relationIds = relationService.addReferences(references);
		return relationIds;
	}	

	@Override
	public String getJsonData() {		
		return this.theJsonData;
	}

	@Override
	public String getBoName() {		
		return this.theBoName;
	}

	@Override
	public String getObjectPk() {		
		return this.theObjectPk;
	}

	@Override
	public List<BoReference> getSourceReferences() {		
		return this.sourceReferences;		
	}
	
	// source --> 是否加载 Source 引用
	private List<BoReference> loadReferences(boolean source) {		
		IRecyclableRelationRowSet relations = relationService.getAllReferencesOf(theObjectPk, source);
		if (relations == null || relations.getRows().isEmpty()) {
			return null;
		}
		
		Map<BoEntityRelationship, List<BoSubjectRelationship>> referenceMap = 
				groupRelationships(relations);		
		List<BoReference> references = toBoReferences(referenceMap);
		
		return references;
		
	}

	private List<BoReference> toBoReferences(Map<BoEntityRelationship, List<BoSubjectRelationship>> referenceMap) {
		List<BoReference> references = new ArrayList<>();
		for (BoEntityRelationship boRelationship : referenceMap.keySet()) {
			references.add(new BoReference(boRelationship, referenceMap.get(boRelationship)));
		}
		return references;
	}

	private Map<BoEntityRelationship, List<BoSubjectRelationship>> groupRelationships(
			IRecyclableRelationRowSet relations) {
		
		Map<BoEntityRelationship, List<BoSubjectRelationship>> referenceMap = new HashMap<>();
		for (IBusinessObjectRow aRow : relations.getRows()) {
			IRecyclableRelationRow aRelation = (IRecyclableRelationRow) aRow;
			BoEntityRelationship entityRelationship = toBoRelationship(aRelation);			
			BoSubjectRelationship subjectRelationship = toSubjectRelationship(aRelation);
			
			List<BoSubjectRelationship> subjectRelationships = referenceMap.get(entityRelationship);
			subjectRelationships = (subjectRelationships == null) ? new ArrayList<BoSubjectRelationship>() : subjectRelationships;
			subjectRelationships.add(subjectRelationship);
			referenceMap.put(entityRelationship, subjectRelationships);			
		}
		return referenceMap;
	}

	private BoSubjectRelationship toSubjectRelationship(IRecyclableRelationRow aRelation) {
		String sourcePk = aRelation.getSourcePk();
		String targetPk = aRelation.getTargetPk();
		BoSubjectRelationship subjectRelationship = new BoSubjectRelationship(sourcePk, targetPk);
		return subjectRelationship;
	}

	private BoEntityRelationship toBoRelationship(IRecyclableRelationRow aRelation) {
		String sourceBo = aRelation.getSourceBo();
		String sourceField = aRelation.getSourceField();
		String targetBo = aRelation.getTargetBo();
		String targetField = "id";			
		BoEntityRelationship entityRelationship = new BoEntityRelationship(sourceBo, sourceField, targetBo, targetField);
		
		return entityRelationship;
	}

	@Override
	public List<BoReference> getTargetReferences() {		
		return this.targetReferences;
	}

	@Override
	public IRecyclableObjectRow get() {		
		return this.theRecyclable;
	}

	@Override
	public void delete() {
		objectService.deleteData(this.theRecyclable);	
		updateObjectStatus(false);
		relationService.deleteInvalidReferences();
	}
}
