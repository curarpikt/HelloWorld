package com.chanapp.chanjet.customer.service.recycle.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.service.recycle.BoEntityRelationship;
import com.chanapp.chanjet.customer.service.recycle.BoReference;
import com.chanapp.chanjet.customer.service.recycle.BoSubjectRelationship;
import com.chanapp.chanjet.web.context.AppContext;
import com.chanapp.chanjet.web.util.QueryLimitUtil;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoDataAccessManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.IBusinessObjectHome;
import com.chanjet.csp.bo.api.IBusinessObjectManager;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.bo.api.IBusinessObjectRowSet;
import com.chanjet.csp.common.base.json.JSONArray;
import com.chanjet.csp.common.base.usertype.PermanentKey;
import com.chanjet.csp.data.api.DataManager;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class DbHelper {
	private static final boolean SOURCE_REFERENCE = true;
	private static final boolean TARGET_REFERENCE = false;
	private static final boolean DO_CLEAR = true;
	private static final boolean DO_UPDATE = false;
	public static final String WEAK_REFERENCE_KEY_TYPE_FIELD_NAME = "relateToType";
	private static final IBusinessObjectManager boManager = AppWorkManager.getBusinessObjectManager();
	private static final BoDataAccessManager dalManager = AppWorkManager.getBoDataAccessManager();
	private static DataManager dataManger = AppWorkManager.getDataManager();
	
	
	public static List<BoReference> loadSourceReferences(IBusinessObjectRow theBoRow, String theSourcePk, List<BoEntityRelationship> relations) {
		return loadReferences(theBoRow, theSourcePk, relations, SOURCE_REFERENCE);		
	}
	public static List<BoReference> loadTargetReferences(IBusinessObjectRow theBoRow, String theSourcePk, 
			List<BoEntityRelationship> relations) {
		return loadReferences(theBoRow, theSourcePk, relations, TARGET_REFERENCE);		
	}
	
	private static List<BoReference> loadReferences(IBusinessObjectRow theBoRow, String theObjectPk, 
			List<BoEntityRelationship> entityRelationships, boolean sourceReference) {
		if (entityRelationships == null || entityRelationships.isEmpty()) {
			return null;
		}		
		
		List<BoReference> references = new ArrayList<>();
		for (BoEntityRelationship entityRelationship : entityRelationships) {			
			IBusinessObjectRowSet boRows = sourceReference ? getSourceRowsOf(theBoRow, entityRelationship) : getTargetRowsOf(theBoRow, entityRelationship);				
			if (boRows == null || boRows.size() == 0) {
				continue;
			}
			
			// subject
			List<BoSubjectRelationship> subjectRelationships = new ArrayList<BoSubjectRelationship>();
			for (IBusinessObjectRow aRow : boRows.getRows()) {
				String pk = getPkOf(aRow).toString();
				BoSubjectRelationship subjectRelation = 
						sourceReference ? new BoSubjectRelationship(pk, theObjectPk) :
								 new BoSubjectRelationship(theObjectPk, pk);
				
				subjectRelationships.add(subjectRelation);
			}
			
			references.add(new BoReference(entityRelationship, subjectRelationships));					
		}
		
		return references;
	}
	
	// 根据 外键引用ID 查询 Source 引用
	private static IBusinessObjectRowSet getSourceRowsOf(IBusinessObjectRow theBoRow, BoEntityRelationship theRelationship) {
		IBusinessObjectRowSet boRows = getReferencesOf(theBoRow, theRelationship, SOURCE_REFERENCE);
		return boRows;
	}

	// 根据 targetID 查询 Target
	private static IBusinessObjectRowSet getTargetRowsOf(IBusinessObjectRow theBoRow, BoEntityRelationship theRelationship) {
		IBusinessObjectRowSet targetBoRows = getReferencesOf(theBoRow, theRelationship, TARGET_REFERENCE);
		return targetBoRows;
	}
	
	public static String getPkOf(IBusinessObjectRow boRow) {
		PermanentKey key = (PermanentKey) boRow.getFieldValue("permanentKey");	
		return key.getValue();		
	}
	
	private static IBusinessObjectRowSet getReferencesOf(IBusinessObjectRow theBoRow, BoEntityRelationship theRelationship, boolean sourceReference) {		
		JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
		Criteria criteria = Criteria.AND();	
		String idField = sourceReference ? theRelationship.getSourceField() : theRelationship.getTargetField();
		Long id;
		if (sourceReference) {			
			id = Long.valueOf(theBoRow.getFieldValue(theRelationship.getTargetField()).toString());
			if (theRelationship.isWeakReference()) {
				criteria.eq(WEAK_REFERENCE_KEY_TYPE_FIELD_NAME, theRelationship.getTargetBo());
			}
		} else {
			String sourceField = theRelationship.getSourceField();			
			id = getIdOf(sourceField, theBoRow);			
		}
		if (id == null) {
			return null;
		}
		criteria.eq(idField, id);	
		jsonQueryBuilder.addCriteria(criteria);
		
		String boName = sourceReference ? theRelationship.getSourceBo() : theRelationship.getTargetBo();
		IBusinessObjectHome boHome = boManager.getPrimaryBusinessObjectHome(boName);
		IBusinessObjectRowSet boRows = QueryLimitUtil.query(jsonQueryBuilder.toJsonQuerySpec(), boHome);
		return boRows;		
	}
	public static Long getIdOf(String referField, IBusinessObjectRow theBoRow) {		
		Object referObject = theBoRow.getFieldValue(referField);
		if (referObject == null) {
			return null;
		}
		
		Long id = null;
		if (referObject instanceof Long) {
			id = (Long) referObject;
		} else if (referObject instanceof Map) {
			Map<String, Object> referMap = (Map) referObject;			
			id = (Long) referMap.get("id");
		}
		
		return id;
	}
	
	// 清除 BoRow 中的外键引用值
	public static void clearReferences(List<BoReference> references,
			IBusinessObjectRow boRowToClear) {
		boRowUpdateReferences(references, boRowToClear, DO_CLEAR);	
		
	}
	// 更新 BoRow 中的外键引用值
	public static void updateReferences(List<BoReference> sourceReferences,
			IBusinessObjectRow boRowToUpdate) {
		boRowUpdateReferences(sourceReferences, boRowToUpdate, DO_UPDATE);	
	}
	// 更新 SourceBoRow 的外键引用值
	private static void boRowUpdateReferences(List<BoReference> references,
			IBusinessObjectRow sourceBoRow, boolean doClear) {
		ReferenceUpdateStrategy strategy = new BoRowReferenceUpdateStrategy(references, sourceBoRow, doClear);
		strategy.updateReferences();
	}
	
	// 清除 DB 中SourceBo的外键值
	public static void clearReferences(List<BoReference> references) {
		Long nullTargetId = null;
		dbUpdateReferences(references, nullTargetId);
	}
	
	public static void updateReferences(List<BoReference> references,
			Long theTargetId) {
		dbUpdateReferences(references, theTargetId);		
	}
	// 更新 外键值
	private static void dbUpdateReferences(List<BoReference> references, Long theTargetId) {
		ReferenceUpdateStrategy strategy = new DatabaseReferenceUpdateStrategy(references, theTargetId);
		strategy.updateReferences();
	}	
	
	public static Long getIdByPk(String boName, String objectPk) {
		IBusinessObjectHome boHome = boManager.getPrimaryBusinessObjectHome(boName);
		
		String jsonQuerySpec = "SELECT c.id FROM " + boHome.getDefinition().getId() 
					+ " AS c WHERE csp_permanent_key_equals(c.permanentKey, '" + objectPk + "') = true";
		BoSession boSession = AppContext.session();
		List<Map<String, Object>> ids =  dalManager.runCQLQuery(boHome, boSession, jsonQuerySpec);
		if (ids == null || ids.isEmpty()) {
			return null;
		}		
		
		return (Long) ids.get(0).get("id");
	}
	public static BoSession getBoSession() {		
		return AppContext.session();
	}
	public static IBusinessObjectHome getBoHome(String boName) {
		return boManager.getPrimaryBusinessObjectHome(boName);
	}
	
	
	public static String toJsonString(Object object) {
		return dataManger.toJSONString(object);
	}

}
