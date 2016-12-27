package com.chanapp.chanjet.customer.service.recycle.db;

import java.util.ArrayList;
import java.util.List;

import com.chanapp.chanjet.customer.service.recycle.BoEntityRelationship;
import com.chanapp.chanjet.customer.service.recycle.BoReference;
import com.chanapp.chanjet.customer.service.recycle.BoSubjectRelationship;
import com.chanapp.chanjet.web.context.AppContext;
import com.chanapp.chanjet.web.jsonquery.JsonQuery;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.IBusinessObjectHome;
import com.google.common.base.Joiner;

public class DatabaseReferenceUpdateStrategy implements ReferenceUpdateStrategy {	
	private List<BoReference> references;
	private Long theTargetId;
	private boolean doClear;
	
	public DatabaseReferenceUpdateStrategy(List<BoReference> references, Long theTargetId) {
		this.references = references;
		this.theTargetId = theTargetId;
		this.doClear = (theTargetId == null);		
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
		
		List<String> sourcePks = new ArrayList<>();
		for (BoSubjectRelationship subject : subjects) {		
			String sourcePk = subject.getSourcePk();
			sourcePks.add("'" + sourcePk + "'");			
			//Long sourceId = DbHelper.getIdByPk(sourceBoName, sourcePk);			
		}
		
		String batchSourcePks = Joiner.on(",").skipNulls().join(sourcePks);		
		String criteria = "csp_permanent_key_in(permanentKey, " + batchSourcePks + ") = true";
		// fieldsToUpdate(relation,sourceBoHome.getDefinition()));
		String jsonQuerySpec = JsonQuery.getInstance().setCriteriaStr(criteria).toString();
		Long targetId = doClear ? null : theTargetId;
		String sourceField = relation.getSourceField();
		String sourceBoName = relation.getSourceBo();
		IBusinessObjectHome sourceBoHome = DbHelper.getBoHome(sourceBoName);
		BoSession boSession = AppContext.session();
		if (relation.isWeakReference()) {
			String relateToType = doClear ? null : relation.getTargetBo();
			sourceBoHome.batchUpdate(boSession, jsonQuerySpec,
					new String[] { sourceField, DbHelper.WEAK_REFERENCE_KEY_TYPE_FIELD_NAME },
					new Object[] { targetId, relateToType }, true);
		} else {
			sourceBoHome.batchUpdate(boSession, jsonQuerySpec, new String[] { sourceField }, new Object[] { targetId }, true);
		}
	}
	

/*	private LinkedHashMap<String, Object> fieldsToUpdate(BoEntityRelationship relation,IBusinessObject bo) {
		LinkedHashMap<String, Object> toUpdate = new LinkedHashMap<>();	
		Long targetId = doClear ? null : theTargetId;		
		String sourceField = relation.getSourceField();				
		if (relation.isWeakReference()) {
			toUpdate.put(sourceField, targetId);
			String relateToType = doClear ? null : relation.getTargetBo();
			toUpdate.put(DbHelper.WEAK_REFERENCE_KEY_TYPE_FIELD_NAME, relateToType);
		} else {
			IBOField boField = bo.getField(sourceField);
			FieldTypeEnum type = boField.getType();
			if(FieldTypeEnum.FOREIGN_KEY.equals(type)){
				Map<String, Object> recycledIdMap = new LinkedHashMap<>();					
				recycledIdMap.put("id", targetId);							
				toUpdate.put(sourceField, recycledIdMap);
			}else{
				toUpdate.put(sourceField, targetId);
			}
		
		}
		
		return toUpdate;
	}
*/

}
