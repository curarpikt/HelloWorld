package com.chanapp.chanjet.customer.service.recycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.chanapp.chanjet.customer.businessobject.api.recyclablerelation.IRecyclableRelationHome;
import com.chanapp.chanjet.customer.businessobject.api.recyclablerelation.IRecyclableRelationRow;
import com.chanapp.chanjet.customer.businessobject.api.recyclablerelation.IRecyclableRelationRowSet;
import com.chanapp.chanjet.web.context.AppContext;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class RecyclableRelationServiceImpl  extends BoBaseServiceImpl<IRecyclableRelationHome, IRecyclableRelationRow, IRecyclableRelationRowSet> 
			implements RecyclableRelationService {
	private static final Long RECYCLABLE = 1L;
	private static final Long RECYCLED = 0L;
	private static final String SOURCE_BO_FIELD = "sourceBo";
	private static final String TARGET_BO_FIELD = "targetBo";
	private static final String TARGET_PK_FIELD = "targetPk";
	private static final String SOURCE_PK_FIELD = "sourcePk";
	private static final String SOURCE_STATUS_FIELD = "sourceStatus";
	private static final String TARGET_STATUS_FIELD = "targetStatus";

	private static RecyclableRelationService instance = new RecyclableRelationServiceImpl();
	public static RecyclableRelationService getInstance() {
		return instance;
	}
	
	@Override
	public void add(String sourceBo, String sourcePk, String targetBo, String targetPk) {
		IRecyclableRelationRow relation = createRow();
		relation.setSourceBo(sourceBo);
		relation.setSourcePk(sourcePk);
		relation.setTargetBo(targetBo);
		relation.setTargetPk(targetPk);
		
		this.upsert(relation);
		Long relationId = relation.getId();		
	}

	@Override
	public IRecyclableRelationRowSet getAllReferencesOf(String objectPk, boolean sourceReference) {		
		return sourceReference ? getAllSourceReferencesOf(objectPk) : getAllTargetReferencesOf(objectPk);
	}	
	
	// 如果查询Source引用，则说明当前pk为targetPK
	private IRecyclableRelationRowSet getAllSourceReferencesOf(String objectPk) {
		Criteria criteria = Criteria.AND();
		criteria.eq(SOURCE_STATUS_FIELD, RECYCLED);	
		criteria.eq(TARGET_PK_FIELD, objectPk);
		
		return getReferencesOf(criteria);
	}	
	
	private IRecyclableRelationRowSet getAllTargetReferencesOf(String objectPk) {
		Criteria criteria = Criteria.AND();
		criteria.eq(TARGET_STATUS_FIELD, RECYCLED);
		criteria.eq(SOURCE_PK_FIELD, objectPk);
		
		return getReferencesOf(criteria);
	}	

	@Override
	public IRecyclableRelationRowSet getSpecificReferencesOf(String objectPk, List<String> referenceBos,
			boolean sourceReference) {		
		return sourceReference ? getSpecificSourceReferencesOf(objectPk, referenceBos) : 
			getSpecificTargetReferencesOf(objectPk, referenceBos);
	}
	
	private IRecyclableRelationRowSet getReferencesOf(Criteria criteria) {		
		JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();		       
		jsonQueryBuilder.addCriteria(criteria);
		return this.query(jsonQueryBuilder.toJsonQuerySpec());
	}

	private IRecyclableRelationRowSet getSpecificSourceReferencesOf(String objectPk, List<String> sourceBos) {
		if (sourceBos == null || sourceBos.isEmpty()) {
			return null;
		}	
		
		Criteria criteria = Criteria.AND();
		criteria.eq(SOURCE_STATUS_FIELD, RECYCLED);
		criteria.in(SOURCE_BO_FIELD, sourceBos.toArray());
		criteria.eq(TARGET_PK_FIELD, objectPk);
		
		return getReferencesOf(criteria);
	}	
	
	private IRecyclableRelationRowSet getSpecificTargetReferencesOf(String objectPk, List<String> targetBos) {
		if (targetBos == null || targetBos.isEmpty()) {
			return null;
		}	
		
		Criteria criteria = Criteria.AND();
		criteria.eq(TARGET_STATUS_FIELD, RECYCLED);
		criteria.in(TARGET_BO_FIELD, targetBos.toArray());
		criteria.eq(SOURCE_PK_FIELD, objectPk);
		
		return getReferencesOf(criteria);
	}	

	@Override
	public List<Long> addReferences(Map<String, List<BoReference>> references) {
		if (references == null || references.isEmpty()) {
			return null;
		}
		
		IRecyclableRelationRowSet rows = insertReferences(references);
		
		List<Long> relationIds = new ArrayList<>();
		for (IBusinessObjectRow row : rows.getRows()) {
			relationIds.add((Long) row.getFieldValue("id"));
		}
		return relationIds;
	}

	private IRecyclableRelationRowSet insertReferences(Map<String, List<BoReference>> references) {
		IRecyclableRelationRowSet rows = createRowSet();
		for (Entry<String, List<BoReference>> referenceEntry : references.entrySet()) {
			boolean source = "source".equals(referenceEntry.getKey());
			List<BoReference> boReferences = referenceEntry.getValue();
			if (boReferences == null || boReferences.isEmpty()) {
				continue;
			}
			for (BoReference boReference : boReferences ) {			
				BoEntityRelationship boRelation = boReference.getEntityRelationship();
				List<BoSubjectRelationship> subjectRelations = boReference.getSubjectRelationships();
				if (subjectRelations == null || subjectRelations.isEmpty()) {
					continue;
				}

				for (BoSubjectRelationship subjectRelation : subjectRelations) {
					IRecyclableRelationRow row = buildRelationFrom(boRelation, subjectRelation, source);
					rows.addRow(row);
				}
			}			
		}
		
		this.batchInsert(rows);
		return rows;
	}

	private IRecyclableRelationRow buildRelationFrom(BoEntityRelationship entityRelation,
			BoSubjectRelationship subjectRelation, boolean source) {
		IRecyclableRelationRow relation = createRow();
		relation.setSourceBo(entityRelation.getSourceBo());
		relation.setSourcePk(subjectRelation.getSourcePk());
		relation.setSourceField(entityRelation.getSourceField());
		relation.setTargetBo(entityRelation.getTargetBo());
		relation.setTargetPk(subjectRelation.getTargetPk());
		
		// 如果 保存的是 Source 引用，说明当前放入回收站的对象是 Target， 则该对象状态为 可回收状态（RECYCLABLE）
		if (source) {
			relation.setSourceStatus(RECYCLED);
			relation.setTargetStatus(RECYCLABLE);
		} else {
			relation.setSourceStatus(RECYCLABLE);
			relation.setTargetStatus(RECYCLED);
		}
		return relation;
	}

	@Override
	public void updateStatusOf(String objectPk, boolean whenAdd) {
		updateSourceStatusOf(objectPk, whenAdd);
		updateTargetStatusOf(objectPk, whenAdd);
	}

	private void updateTargetStatusOf(String objectPk, boolean whenAdd) {
		updateStatusOf(objectPk, false, whenAdd);		
	}

	private void updateSourceStatusOf(String objectPk, boolean whenAdd) {
		updateStatusOf(objectPk, true, whenAdd);		
	}

	private void updateStatusOf(String objectPk, boolean sourceStatus, boolean whenAdd) {
		Long oldStatus = whenAdd ? RECYCLED : RECYCLABLE;
		Long newStatus = whenAdd ? RECYCLABLE : RECYCLED;		
		String pkField = sourceStatus ? SOURCE_PK_FIELD : TARGET_PK_FIELD;
		String statusField = sourceStatus ? SOURCE_STATUS_FIELD : TARGET_STATUS_FIELD;
		
		JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
		Criteria criteria = Criteria.AND();		
		criteria.eq(pkField, objectPk).eq(statusField, oldStatus);						       
		jsonQueryBuilder.addCriteria(criteria);
		BoSession boSession = AppContext.session();
		getBusinessObjectHome().batchUpdate(boSession, jsonQueryBuilder.toJsonQuerySpec(), new String[] { statusField }, new Object[] { newStatus }, true);
	}

	@Override
	public void deleteInvalidReferences() {
		JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
		Criteria criteria = Criteria.AND();		
		criteria.eq(SOURCE_STATUS_FIELD, RECYCLED).eq(TARGET_STATUS_FIELD, RECYCLED);						       
		jsonQueryBuilder.addCriteria(criteria);
		BoSession boSession = AppContext.session();
		getBusinessObjectHome().batchDelete(boSession, jsonQueryBuilder.toJsonQuerySpec(), true);	
	}

	@Override
	public void deleteAll() {
		JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
		Criteria criteria = Criteria.NOT();		
		criteria.empty("id");		
		jsonQueryBuilder.addCriteria(criteria);
		BoSession boSession = AppContext.session();
		getBusinessObjectHome().batchDelete(boSession, jsonQueryBuilder.toJsonQuerySpec(), true);	
	}

	@Override
	public void deleteReferencesOf(List<String> deletedPks) {
		if (deletedPks == null || deletedPks.isEmpty()) {
			return;
		}
		JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
		Criteria criteria = Criteria.OR();		
		criteria.in(SOURCE_PK_FIELD, deletedPks.toArray());	
		criteria.in(TARGET_PK_FIELD, deletedPks.toArray());
		jsonQueryBuilder.addCriteria(criteria);
		BoSession boSession = AppContext.session();
		getBusinessObjectHome().batchDelete(boSession, jsonQueryBuilder.toJsonQuerySpec(), true);
	}

	@Override
	public void updateObjectPks(Map<String, String> pkMap) {
		if (pkMap == null || pkMap.isEmpty()) {
			return;
		}
		
		for (String oldPk : pkMap.keySet()) {
			String newPk = pkMap.get(oldPk);
			updateObjectPkOf(oldPk, newPk, true);	
			updateObjectPkOf(oldPk, newPk, false);
		}		
		
	}

	private void updateObjectPkOf(String oldPk, String newPk, boolean sourcePk) {			
		String pkField = sourcePk ? SOURCE_PK_FIELD : TARGET_PK_FIELD;
		
		JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
		Criteria criteria = Criteria.AND();		
		criteria.eq(pkField, oldPk);						       
		jsonQueryBuilder.addCriteria(criteria);
		BoSession boSession = AppContext.session();
		getBusinessObjectHome().batchUpdate(boSession, jsonQueryBuilder.toJsonQuerySpec(), new String[] { pkField }, new Object[] { newPk }, true);
	}

	@Override
	public IRecyclableRelationRow get(Long relationId) {		
		return this.query(relationId);
	}



}
