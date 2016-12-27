package com.chanapp.chanjet.customer.service.recycle.db;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.service.recycle.BoEntityRelationship;
import com.chanapp.chanjet.customer.service.recycle.BoReference;
import com.chanapp.chanjet.customer.service.recycle.BoSubjectRelationship;
import com.chanjet.csp.bo.api.IBusinessObjectRow;

public class BoRowReferenceUpdateStrategy implements ReferenceUpdateStrategy {
	private List<BoReference> references;
	private IBusinessObjectRow sourceBoRow;
	private boolean doClear;
	
	public BoRowReferenceUpdateStrategy(List<BoReference> references,
			IBusinessObjectRow sourceBoRow, boolean doClear) {
		this.references = references;
		this.sourceBoRow = sourceBoRow;
		this.doClear = doClear;
	}

	@Override
	public void updateReferences() {
		if (references == null || references.isEmpty()) {
			return;
		}	
		
		for (BoReference boReference : references) {
			BoEntityRelationship boRelation = boReference.getEntityRelationship();
			List<BoSubjectRelationship> subjectRelations = boReference.getSubjectRelationships();
			doUpdate(boRelation, subjectRelations);
		}
	}
	
	private void doUpdate(BoEntityRelationship relation, List<BoSubjectRelationship> subjects) {
		if (relation == null || subjects == null || subjects.isEmpty()) {
			return;
		}
		
		for (BoSubjectRelationship subject : subjects) {
			String sourceField = relation.getSourceField();
			if (doClear) {
				sourceBoRow.setFieldValue(sourceField, null);				
			} else {
				String targetBoName = relation.getTargetBo();			
				String targetPk = subject.getTargetPk();
				
				Long targetId = DbHelper.getIdByPk(targetBoName, targetPk);
				if (targetId == null) {
					continue;					
				}
				
				Map<String, Object> targetIdMap = new LinkedHashMap<>();						
				targetIdMap.put("id", targetId);				
				sourceBoRow.setFieldValue(sourceField, targetIdMap);				
			}
			
			if (relation.isWeakReference()) {
				String relateToType = doClear ? null : relation.getTargetBo();
				sourceBoRow.setFieldValue(DbHelper.WEAK_REFERENCE_KEY_TYPE_FIELD_NAME, relateToType);
			}			
		}		
	}
	
}
