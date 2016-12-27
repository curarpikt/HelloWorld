package com.chanapp.chanjet.customer.service.recycle.db;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.eventhandler.deletehandler.CustomerDeleteHandler;
import com.chanapp.chanjet.customer.service.recover.RecoverManager;
import com.chanapp.chanjet.customer.service.recycle.BoEntityReference;
import com.chanapp.chanjet.customer.service.recycle.BoEntityReferenceImpl;
import com.chanapp.chanjet.customer.service.recycle.BoEntityRelationship;
import com.chanapp.chanjet.customer.service.recycle.BoReference;
import com.chanapp.chanjet.web.context.AppContext;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.IBusinessObjectHome;
import com.chanjet.csp.bo.api.IBusinessObjectManager;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.util.CspLogLevel;
import com.chanjet.csp.common.base.util.ExceptionUtils;
import com.chanjet.csp.data.api.DataManager;

public class DatabaseObjectReferenceImpl implements DatabaseObjectReference {
	private Long theObjectId;
	private String theObjectPk;
	private String theBoName;
	private String theObjectName;
	private IBusinessObjectRow theBoRow;
	private IBusinessObjectHome theBoHome;

	private IBusinessObjectManager boManager = AppWorkManager.getBusinessObjectManager();
	private BoSession boSession = AppContext.session();
	private DataManager dataManger = AppWorkManager.getDataManager();	

	//private BoEntityReference boReference = DefaultBoEntityReference.getInstance();
	private BoEntityReference boReference = new BoEntityReferenceImpl();
	private List<BoReference> sourceReferences; 
	private List<BoReference> targetReferences; 	
	
	private boolean deletedByEvent = false;
	
	private static final Logger logger = LoggerFactory.getLogger(CustomerDeleteHandler.class);

	/***************************** 删除  *****************************************/
	public DatabaseObjectReferenceImpl(String boName, Long objectId) {
		this.theBoName = boName;
		this.theObjectId = objectId;
		this.theBoHome = boManager.getPrimaryBusinessObjectHome(theBoName);	
		this.theBoRow = theBoHome.query(boSession, theObjectId);
		this.theObjectPk = DbHelper.getPkOf(theBoRow);
		this.theObjectName = (String) theBoRow.getFieldValue("name");
		
		// 加载引用关系
		loadReferences();
	}
	
	public DatabaseObjectReferenceImpl(String boName, Long objectId, IBusinessObjectRow boRow) {
		this.theBoName = boName;
		this.theObjectId = objectId;		
		this.theBoRow = boRow;	
		this.theBoHome = boManager.getPrimaryBusinessObjectHome(theBoName);			
		this.theObjectPk = DbHelper.getPkOf(theBoRow);
		this.theObjectName = (String) theBoRow.getFieldValue("name");
		this.deletedByEvent  = true;
		
		// 加载引用关系
		loadReferences();
	}

	private void loadReferences() {
		List<BoEntityRelationship> sourceRelations = boReference.getSourcesOf(theBoName);
		this.sourceReferences = 
				DbHelper.loadSourceReferences(theBoRow, theObjectPk, sourceRelations);
		
		List<BoEntityRelationship> targetBoRelations = boReference.getTargetsOf(theBoName);
		this.targetReferences = 
				DbHelper.loadTargetReferences(theBoRow, theObjectPk, targetBoRelations);
		
	}

	@Override
	public List<BoReference> getSourceReferences() {		
		return this.sourceReferences;
	}

	@Override
	public List<BoReference> getTargetReferences() {		
		return this.targetReferences;
	}

	@Override
	public void delete() {
		clearSourceReferences();
		if (deletedByEvent) {
			return;
		}
		try{
			theBoHome.delete(boSession, theObjectId);	
		}catch(Exception e){
			ExceptionUtils.logExceptionStackTrace(e, logger, CspLogLevel.INFO);
			throw new AppException("该客户已有关联数据，不能被删除");
		}
			
	}

	private void clearSelfReferences() {
		DbHelper.clearReferences(targetReferences, theBoRow);		
	}

	private void clearSourceReferences() {
		DbHelper.clearReferences(sourceReferences);
	}
	
	@Override
	public boolean isReferred() {		
		return (sourceReferences != null && !sourceReferences.isEmpty());
	}
	
	@Override
	public String getJsonData() {
		// 删除自身引用关系
		clearSelfReferences();
		Map<String, Object> boData = theBoRow.getAllFieldValues();
		return dataManger.toJSONString(boData);
	}

	@Override
	public Object getFieldValue(String fieldName) {		
		return theBoRow.getFieldValue(fieldName);
	}

	@Override
	public String getPk() {		
		return this.theObjectPk;
	}

	@Override
	public Long getObjectId() {		
		return this.theObjectId;
	}
	
	@Override
	public String getName() {		
		return this.theObjectName;
	}

	/***************************** 还原  *****************************************/
	public DatabaseObjectReferenceImpl(String boName, String jsonStringData, 
			List<BoReference> sourceReferences, List<BoReference> targetReferences) {
		this.theBoName = boName;
		this.theBoHome = boManager.getPrimaryBusinessObjectHome(boName);
		this.sourceReferences = sourceReferences;
		this.targetReferences = targetReferences;
		
		LinkedHashMap<String, Object> jsonData = dataManger.fromJSONString(jsonStringData, LinkedHashMap.class);		
		this.theBoRow = theBoHome.constructBORowForInsert(boSession, jsonData);	
	}
	
	@Override
	public Long save() {
		// 还原自身引用
		recoverSelfReferences();
		IBusinessObjectRow newBoRow = RecoverManager.recoverEntityRow(theBoName, theBoRow);
		this.theObjectId = (Long) newBoRow.getFieldValue("id");
		this.theObjectPk = DbHelper.getPkOf(newBoRow);
		// 还原 Source 引用
		recoverSourceReferences();
		return theObjectId;
	}	

	private void recoverSelfReferences() {
		DbHelper.updateReferences(targetReferences, theBoRow);
	}

	private void recoverSourceReferences() {
		Long theTargetId = this.theObjectId;
		DbHelper.updateReferences(sourceReferences, theTargetId);
	}	
}
