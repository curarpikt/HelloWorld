package com.chanapp.chanjet.customer.service.recycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.businessobject.api.recyclableobject.IRecyclableObjectRow;
import com.chanapp.chanjet.customer.businessobject.api.recyclableobject.IRecyclableObjectRowSet;
import com.chanapp.chanjet.customer.constant.EO;
import com.chanapp.chanjet.customer.handler.CustomAppContextHandler;
import com.chanapp.chanjet.customer.service.recycle.db.DatabaseBatchObjectReference;
import com.chanapp.chanjet.customer.service.recycle.db.DatabaseObjectReference;
import com.chanapp.chanjet.customer.service.recycle.db.DatabaseObjectReferenceImpl;
import com.chanapp.chanjet.customer.util.EnterpriseUtil;
import com.chanapp.chanjet.customer.util.ListUtils;
import com.chanapp.chanjet.customer.vo.system.AppUser;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.context.AppContext;
import com.chanapp.chanjet.web.util.QueryLimitUtil;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.BoTransactionManager;
import com.chanjet.csp.bo.api.IBusinessObjectHome;
import com.chanjet.csp.bo.api.IBusinessObjectManager;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.bo.api.IBusinessObjectRowSet;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.common.base.util.CspLogLevel;
import com.chanjet.csp.common.base.util.ExceptionUtils;
import com.chanjet.csp.common.base.util.TransactionTracker;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class RecyclableBinImpl implements RecyclableBin {
    private final static Logger logger = LoggerFactory.getLogger(RecyclableBinImpl.class);
	private RecyclableObjectService objectService = RecyclableObjectServiceImpl.getInstance();
	private RecyclableRelationService relationService = RecyclableRelationServiceImpl.getInstance();
	
	@Override
	public Map<String, Object> put(String boName, Long objectId) {
		return put(boName, objectId, null);
	}	
	
	@Override
	public Map<String, Object> put(String boName, Long objectId, Long operationUser) {
		// cache data and delete from DB	
		DatabaseObjectReference dbDataReference = new DatabaseObjectReferenceImpl(boName, objectId);
		dbDataReference.delete();
		
		String jsonData = dbDataReference.getJsonData();
		String objectPk = dbDataReference.getPk();
		String objectName = dbDataReference.getName();
		List<BoReference> sourceReferences = dbDataReference.getSourceReferences();
		List<BoReference> targetReferences = dbDataReference.getTargetReferences();
		if(operationUser == null){
			operationUser = AppWorkManager.getCurrAppUserId();// EnterpriseContext.getCurrentUser().getUserLongId();
		}
		
		// add data and references to recyclable bin
		RecyclableBinObjectReference rbDataReference = 
				new RecyclableBinObjectReferenceImpl(boName, objectPk, jsonData, objectName, operationUser, sourceReferences, targetReferences);
		Map<String, Object> result = rbDataReference.save();

		return result;
	}
	
	@Override
	public Map<String, Object> put(String boName, Long objectId, IBusinessObjectRow boRow, Long userId) {
		// cache data and delete from DB	
		DatabaseObjectReference dbDataReference = new DatabaseObjectReferenceImpl(boName, objectId, boRow);
		dbDataReference.delete();
		
		// data and references to be saved in recyclable bin
		String jsonData = dbDataReference.getJsonData();
		String objectPk = dbDataReference.getPk();
		String objectName = dbDataReference.getName();		
		List<BoReference> sourceReferences = dbDataReference.getSourceReferences();
		List<BoReference> targetReferences = dbDataReference.getTargetReferences();
		
		// save data in recyclable bin 
		RecyclableBinObjectReference rbDataReference = 
				new RecyclableBinObjectReferenceImpl(boName, objectPk, jsonData, objectName, userId, sourceReferences, targetReferences);
		Map<String, Object> result = rbDataReference.save();

		return result;
	}

	
	@Override
	public Map<String, Object> putBatch(String boName, List<Long> objectIds, String reason) {
		// cache batch data and delete from DB
		DatabaseBatchObjectReference dbBatchReference = new DatabaseBatchObjectReference(boName, objectIds);
		dbBatchReference.delete();
		
		Map<String, String> batchJsonData = dbBatchReference.getJsonDataMap();
		Map<String, String> batchNames = dbBatchReference.getNameMap();
		Map<String, List<BoReference>> batchSourceReferences = dbBatchReference.getSourceReferenceMap();
		Map<String, List<BoReference>> batchTargetReferences = dbBatchReference.getTargetReferenceMap();
		
		// save batch data and references in recyclable bin
		RecyclableBinBatchObjectReference rbBatchReference = 
				new RecyclableBinBatchObjectReference(boName, batchJsonData, batchNames, reason, batchSourceReferences, batchTargetReferences);
		Map<String, Object> result = rbBatchReference.save();
		
		return result;
		
	}
	
	@Override
	public List<Long> recycle(Long recyclableObjectId) {
		// cache data from recyclable bin
		RecyclableBinBatchObjectReference rbBatchReference = new RecyclableBinBatchObjectReference(recyclableObjectId);
		
		Map<String, String> jsonDataMap = rbBatchReference.getJsonDataMap();
		String boName = rbBatchReference.getBoName();			
		Map<String, List<BoReference>> sourceReferenceMap = rbBatchReference.getSourceReferenceMap();
		Map<String, List<BoReference>> targetReferenceMap = rbBatchReference.getTargetReferenceMap();
		
		// save data in DB
		DatabaseBatchObjectReference dbBatchReference = 
				new DatabaseBatchObjectReference(boName, jsonDataMap, sourceReferenceMap, targetReferenceMap);
		List<Long> recycledObjectIds = dbBatchReference.save();
		
		// update PKs and delete data from recyclable bin
		Map<String, String> pkMap = dbBatchReference.getPkMap();
		rbBatchReference.deleteAndUpdatePks(pkMap);
		
		return recycledObjectIds;
	}

	/*@Override
	public Long recycle(Long recyclableObjectId) {
		RecyclableBinObjectReference rbDataReference = new RecyclableBinObjectReferenceImpl(recyclableObjectId);
		
		String jsonData = rbDataReference.getJsonData();
		String boName = rbDataReference.getBoName();		
		// recycle the object		
		List<BoReference> sourceReferences = rbDataReference.getSourceReferences();
		List<BoReference> targetReferences = rbDataReference.getTargetReferences();
				
		DatabaseObjectReference dbDataReference = 
				new DatabaseObjectReferenceImpl(boName, jsonData, sourceReferences, targetReferences);
		Long recycledObjectId = dbDataReference.save();
		
		rbDataReference.delete();
		
		return recycledObjectId;
	}*/	

/*	@Override
	public Object get(Long recyclableObjectId) {		
		RecyclableBinObjectReference rbDataReference = new RecyclableBinObjectReferenceImpl(recyclableObjectId);
		IRecyclableObjectRow theRecyclable = rbDataReference.get();
		return theRecyclable;
	}*/

	@Override
	public int clearAll() {
		objectService.deleteAll();
		relationService.deleteAll();
		return 0;
	}


	@Override
	public void init() {
        String key = "CUSTOMER_CLEANDELETED_DATA";
        String flag ="INITED";
/*        String upgraded = AppWorkManager.getDataAuthManager().getPriorSetting(AppWorkManager.getCurrentAppId())
                .getApplicationValue(key);*/
        String upgraded = null;
        int batchNum = 1000;
        //只初始化一次
        if(upgraded==null||!upgraded.equals(flag)){
            //初始化其他的记录
            for(String boName:EO.UnNeedRecycles){    
            	List<Long> ids = getDeletedIds(boName);  
            	List<List<Long>> subList = ListUtils.split(ids, batchNum);
            	for(List<Long> innerIds:subList){
            		cleanDeletedRecordByIds(boName,innerIds);
            	}           	
            } 
    		//初始化需要进入回收站的记录
            for(String boName:EO.NeedRecycles){    
            	List<Long> ids = getDeletedIds(boName);   
            	List<List<Long>> subList = ListUtils.split(ids, batchNum);
            	for(List<Long> innerIds:subList){
            		recycleByIds(boName,innerIds);
            	}                 	
            }	
    
         //   AppWorkManager.getDataAuthManager().getPriorSetting(AppWorkManager.getCurrentAppId()).setApplicationValue(key, flag);
        }  
	}
	
	private void recycleByIds(String boName, List<Long> ids) {
		if(ids==null||ids.size()==0)
			return;
		RecyclableBin service = new RecyclableBinImpl();
		BoSession session = AppContext.session();
		BoTransactionManager tranxManager = AppWorkManager.getBoTransactionManager();
		TransactionTracker tracker = null;
		Long start = null;
		int index = 0;
		Long userId = null;
		try {
			List<AppUser> appUsers = EnterpriseUtil.findSuperAppUserByAppId(AppWorkManager.getCurrentAppId());
			if(appUsers!=null&&appUsers.size()>0){
				userId = appUsers.get(0).getUser().getId();
			}
			start = System.currentTimeMillis();
			tracker = tranxManager.beginTransaction(session);
			for (Long id : ids) {
				service.put(boName, id, userId);
				index++;
			}
			logger.info("recycleBin recycleByIds " + index + " time = " + (System.currentTimeMillis() - start) + " ms");
			tranxManager.commitTransaction(session, tracker);
			AppWorkManager.setJMXAppProgress(50);
		} catch (Exception e) {
			ExceptionUtils.logExceptionStackTrace(e, logger, CspLogLevel.INFO);
			if (tracker != null && session != null && session.getTransaction() != null && session.getTransaction().isActive()) {
				tranxManager.rollbackTransaction(session);
			}
		}
	}

	private void cleanDeletedRecordByIds(String boName, List<Long> ids) {
		if(ids==null||ids.size()==0)
			return;
		BoSession session = AppContext.session();
		BoTransactionManager tranxManager = AppWorkManager.getBoTransactionManager();
		TransactionTracker tracker = null;
		Long start = null;
		int index = 0;
		try {
			start = System.currentTimeMillis();
			tracker = tranxManager.beginTransaction(session);
	      
			for (Long id : ids) {
				DatabaseObjectReference reference = new DatabaseObjectReferenceImpl(boName, id);
				reference.delete();
				index++;
			}
			logger.info("recycleBin recycleByIds " + index + " time = " + (System.currentTimeMillis() - start) + " ms");
			tranxManager.commitTransaction(session, tracker);
			AppWorkManager.setJMXAppProgress(50);
		} catch (Exception e) {
			ExceptionUtils.logExceptionStackTrace(e, logger, CspLogLevel.INFO);
			if (tracker != null && session != null && session.getTransaction() != null && session.getTransaction().isActive()) {
				tranxManager.rollbackTransaction(session);
			}
		}
	}
		
	private List<Long> getDeletedIds(String boName){
		List<Long> ids = new ArrayList<Long>();
        IBusinessObjectManager boManager = AppWorkManager.getBusinessObjectManager();	
        IBusinessObjectHome  home = boManager.getPrimaryBusinessObjectHome(boName);    
        Criteria criteria = Criteria.AND();
        criteria.eq(SC.isDeleted, true);
        String jsonQuerySpec = JsonQueryBuilder.getInstance().addCriteria(criteria).addFields(SC.id).toJsonQuerySpec();
        IBusinessObjectRowSet rowSet = QueryLimitUtil.query(jsonQuerySpec, home);
        if(rowSet!=null&&rowSet.getRows()!=null){
        	for(IBusinessObjectRow row:rowSet.getRows()){     		
        		ids.add((Long)row.getFieldValue(SC.id));
        	}
        }        
        return ids;
	}


	@Override
	public int delete(List<Long> recyclableObjectIds) {
		List<String> deletedPks = objectService.delete(recyclableObjectIds);
		relationService.deleteReferencesOf(deletedPks);
		return recyclableObjectIds.size();
	}
	
	// 只查询可回收对象的数据
	@Override
	public List<BatchRecyclableObject> get(String boName, List<Long> operationUserIds, 
			Long startTime, Long endTime, Integer pageNo, Integer itemsPerPage) {
		List<String> nullBos = null;
		return getWithReferences(boName, nullBos, nullBos, operationUserIds, startTime, endTime, pageNo, itemsPerPage);
	}
	
	// 查询可回收对象的数据 和 指定的引用关系
	@Override
	public List<BatchRecyclableObject> getWithReferences(String boName, List<String> sourceBos, List<String> targetBos,
			List<Long> operationUserIds, Long startTime, Long endTime, Integer pageNo, Integer itemsPerPage) {		
		IRecyclableObjectRowSet recyclables = objectService.get(boName, operationUserIds, startTime, endTime, pageNo, itemsPerPage);
		if (recyclables == null || recyclables.getRows().isEmpty()) {
			 return null;
		 }
		 
		 List<BatchRecyclableObject> recyclableObjects = new ArrayList<>();
		 for (IBusinessObjectRow row : recyclables.getRows()) {
			 IRecyclableObjectRow recyclable = (IRecyclableObjectRow) row;
			 BatchRecyclableObject recyclableObject = getReferencesOf(recyclable, boName, sourceBos, targetBos);			 
			 recyclableObjects.add(recyclableObject);
		 }
		 return recyclableObjects;
	}
	
	// 查询可回收对象的数据 和 所有的引用关系
	@Override
	public BatchRecyclableObject get(Long recyclableId) {
		RecyclableBinBatchObjectReference rbReference = 
				new RecyclableBinBatchObjectReference(recyclableId);
		return new BatchRecyclableObject(rbReference.getBoName(), rbReference.getId(), rbReference.getJsonDataMap(), rbReference.getNameMap(),				
				rbReference.getSourceReferenceMap(), rbReference.getTargetReferenceMap());
	}
	
	private BatchRecyclableObject getReferencesOf(IRecyclableObjectRow recyclable, String boName, 
			List<String> sourceBos,	List<String> targetBos) {
		RecyclableBinBatchObjectReference rbReference = 
				new RecyclableBinBatchObjectReference(recyclable, sourceBos, targetBos);
		Map<String, String> operUser = (Map<String, String>) recyclable.getFieldValue("operUser");
		
		return new BatchRecyclableObject(boName, recyclable.getId(), rbReference.getJsonDataMap(), rbReference.getNameMap(),
				recyclable.getReason(), operUser, recyclable.getOperationTime(), 
				rbReference.getSourceReferenceMap(), rbReference.getTargetReferenceMap());

	}

	@Override
	public List<Long> getOperationUsers(String boName) {		
		return objectService.getOperationUsers(boName);
	}
	
	@Override
	public Integer getCount(String boName, List<Long> operationUserIds, 
			Long startTime, Long endTime){
		return objectService.getCount(boName, operationUserIds, startTime, endTime);
	}


}
