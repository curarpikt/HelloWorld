package com.chanapp.chanjet.customer.service.recycle;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.chanapp.chanjet.customer.businessobject.api.recyclableobject.IRecyclableObjectRow;
import com.chanapp.chanjet.customer.businessobject.api.recyclablerelation.IRecyclableRelationRow;
import com.chanapp.chanjet.customer.businessobject.api.recyclablerelation.IRecyclableRelationRowSet;
import com.chanapp.chanjet.customer.service.recycle.db.DbHelper;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class RecyclableBinBatchObjectReference {
	private static final boolean WHEN_ADD = true;
	private static final boolean WHEN_DELETE = false;
	private static final boolean SOURCE_REFERENCE = true;
	private static final boolean TARGET_REFERENCE = false;

	private List<String> objectPks = Lists.newArrayList();
	// keyed by objectPK
	private Map<String, String> jsonDataMap = Maps.newHashMap();
	private Map<String, String> nameMap = Maps.newHashMap();
	private Map<String, List<BoReference>> sourceReferenceMap = Maps.newHashMap();
	private Map<String, List<BoReference>> targetReferenceMap = Maps.newHashMap();
	private Long theRecyclableId;
	private String theBoName;
	private String reason;

	private RecyclableObjectService objectService = RecyclableObjectServiceImpl.getInstance();
	private RecyclableRelationService relationService = RecyclableRelationServiceImpl.getInstance();
	private IRecyclableObjectRow theRecyclable;		

	/************************ 放入回收站  
	 * @param reason ********************************************/
	public RecyclableBinBatchObjectReference(String boName,
			Map<String, String> jsonDataMap, Map<String, String> nameMap, 
			String reason, Map<String, List<BoReference>> sourceReferenceMap,
			Map<String, List<BoReference>> targetReferenceMap) {
		this.theBoName = boName;
		this.jsonDataMap = jsonDataMap;
		this.nameMap = nameMap;
		this.reason = reason;
		this.sourceReferenceMap = sourceReferenceMap;
		this.targetReferenceMap = targetReferenceMap;

		if (jsonDataMap != null && !jsonDataMap.isEmpty()) {
			this.objectPks = Lists.newArrayList(jsonDataMap.keySet());
		}
	}	

	public Map<String, Object> save() {
		if (objectPks == null || objectPks.isEmpty()) {
			return null;
		}

		// 保存引用关系
		List<Long> relationIds = addReferences();		
		this.theRecyclableId = saveData(); 
		// 更新引用中对象状态
		updateObjectStatus(WHEN_ADD);

		Map<String, Object> result = new HashMap<>();
		result.put("recyclableId", theRecyclableId);
		result.put("relationIds", relationIds);
		return result;
	}

	private Long saveData() {
		Long operationUser = EnterpriseContext.getCurrentUser().getUserLongId();
		Timestamp operationTime = new Timestamp(new Date().getTime());

		String batchJsonData = DbHelper.toJsonString(jsonDataMap);
		String batchNames = DbHelper.toJsonString(nameMap);
		String batchObjectPks = Joiner.on(",").skipNulls().join(objectPks);		
		Long recyclableId = objectService.put(theBoName, batchObjectPks, batchJsonData, batchNames, reason, operationUser, operationTime);
		return recyclableId;
	}

	private List<Long> addReferences() {
		List<BoReference> batchSourceReferences = Lists.newArrayList();
		for (String objectPk : objectPks) {
			batchSourceReferences.addAll(sourceReferenceMap.get(objectPk));
		}
		List<BoReference> batchTargetReferences = Lists.newArrayList();
		for (String objectPk : objectPks) {
			batchTargetReferences.addAll(targetReferenceMap.get(objectPk));
		}

		Map<String, List<BoReference>> references = Maps.newHashMap();
		references.put("source", batchSourceReferences);
		references.put("target", batchTargetReferences);
		List<Long> relationIds = relationService.addReferences(references);
		return relationIds;
	}

	private void updateObjectStatus(boolean whenAdd) {
		for (String objectPk : objectPks) {
			relationService.updateStatusOf(objectPk, whenAdd);		
		}		
	}

	/************************  还原  ********************************************/
	public RecyclableBinBatchObjectReference(Long recyclableId) {
		loadData(recyclableId);
		loadAllReferences();
	}
	
	public RecyclableBinBatchObjectReference(Long recyclableId, List<String> sourceBos, List<String> targetBos) {
		loadData(recyclableId);
		loadSpecificReferences(sourceBos, targetBos);
	}
	
	public RecyclableBinBatchObjectReference(IRecyclableObjectRow recyclable, List<String> sourceBos, List<String> targetBos) {
		loadData(recyclable);
		loadSpecificReferences(sourceBos, targetBos);
	}

	private void loadData(Long recyclableId) {
		this.theRecyclableId = recyclableId;
		this.theRecyclable = objectService.get(recyclableId);
		initData();
	}
	
	private void loadData(IRecyclableObjectRow recyclable) {
		this.theRecyclable = recyclable;
		this.theRecyclableId = recyclable.getId();		
		initData();
	}

	private void initData() {
		String batchJsonData = theRecyclable.getJsonData();
		this.jsonDataMap = AppWorkManager.getDataManager().fromJSONString(batchJsonData, Map.class);
		String batchNames = theRecyclable.getName();
		this.nameMap =  AppWorkManager.getDataManager().fromJSONString(batchNames, Map.class);	
		this.objectPks = Splitter.on(",").splitToList(theRecyclable.getObjectPk());
		this.theBoName = theRecyclable.getBoName();
	}
	
	public void deleteAndUpdatePks(Map<String, String> pkMap) {
		objectService.deleteData(this.theRecyclable);
		// 回写状态
		updateObjectStatus(WHEN_DELETE);
		relationService.updateObjectPks(pkMap);
		relationService.deleteInvalidReferences();		
	}

	private void loadAllReferences() {	
		for (String objectPk : objectPks) {
			List<BoReference> sourceReferences = loadAllReferencesOf(objectPk, SOURCE_REFERENCE);
			sourceReferenceMap.put(objectPk, sourceReferences);
			List<BoReference> targetReferences = loadAllReferencesOf(objectPk, TARGET_REFERENCE);
			targetReferenceMap.put(objectPk, targetReferences);
		}		
	}
	
	private void loadSpecificReferences(List<String> sourceBos, List<String> targetBos) {
		for (String objectPk : objectPks) {
			List<BoReference> sourceReferences = loadSpecificReferencesOf(objectPk, sourceBos, SOURCE_REFERENCE);
			sourceReferenceMap.put(objectPk, sourceReferences);
			List<BoReference> targetReferences = loadSpecificReferencesOf(objectPk, targetBos, TARGET_REFERENCE);
			targetReferenceMap.put(objectPk, targetReferences);
		}			
	}


	private List<BoReference> loadSpecificReferencesOf(String objectPk, List<String> referenceBos,
			boolean sourceReference) {
		IRecyclableRelationRowSet relations = relationService.getSpecificReferencesOf(objectPk, referenceBos, sourceReference);
		if (relations == null || relations.getRows().isEmpty()) {
			return null;
		}

		Map<BoEntityRelationship, List<BoSubjectRelationship>> referenceMap = 
				groupRelationships(relations);		
		List<BoReference> references = toBoReferences(referenceMap);

		return references;
	}

	// source --> 是否加载 Source 引用
	private List<BoReference> loadAllReferencesOf(String objectPk, boolean sourceReference) {		
		IRecyclableRelationRowSet relations = relationService.getAllReferencesOf(objectPk, sourceReference);
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

	public Map<String, String> getJsonDataMap() {		
		return this.jsonDataMap;
	}

	public String getBoName() {		
		return this.theBoName;
	}

	public Map<String, List<BoReference>> getSourceReferenceMap() {	
		return this.sourceReferenceMap;
	}

	public Map<String, List<BoReference>> getTargetReferenceMap() {		
		return this.targetReferenceMap;
	}

	public Long getId() {
		return this.theRecyclableId;
	}

	public Map<String, String> getNameMap() {		
		return this.nameMap;
	}


}
