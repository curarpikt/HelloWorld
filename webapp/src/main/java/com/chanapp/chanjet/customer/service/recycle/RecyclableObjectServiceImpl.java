package com.chanapp.chanjet.customer.service.recycle;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.recyclableobject.IRecyclableObjectHome;
import com.chanapp.chanjet.customer.businessobject.api.recyclableobject.IRecyclableObjectRow;
import com.chanapp.chanjet.customer.businessobject.api.recyclableobject.IRecyclableObjectRowSet;
import com.chanapp.chanjet.web.context.AppContext;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class RecyclableObjectServiceImpl extends BoBaseServiceImpl<IRecyclableObjectHome, IRecyclableObjectRow, IRecyclableObjectRowSet> 
			implements RecyclableObjectService {
	private static final String ID_FIELD = "id";
	private static final String OPERATION_USER_FIELD = "operUser";
	private static final String OPERATION_TIME_FIELD = "operationTime";
	private static final String BO_NAME_FIELD = "boName";

	
	private static RecyclableObjectService instance = new RecyclableObjectServiceImpl();
	public static RecyclableObjectService getInstance() {
		return instance;
	}
	
	@Override
	public Long put(String boName, String objectPk,	String jsonData, String objectName, 
			String reason, Long operationUser, Timestamp operationTime) {
		
		IRecyclableObjectRow recyclable = 
				recyclableObjectOf(boName, objectPk, jsonData, objectName, reason, operationUser, operationTime);
		this.upsert(recyclable);		
		Long recyclableId = recyclable.getId();		
		return recyclableId;
	}

	private IRecyclableObjectRow recyclableObjectOf(String boName, String objectPk, String jsonData, String objectName, 
			String reason, Long operationUser,	Timestamp operationTime) {
		IRecyclableObjectRow recyclable = createRow();
		recyclable.setBoName(boName);
		recyclable.setObjectPk(objectPk);
		recyclable.setJsonData(jsonData);
		recyclable.setName(objectName);
		recyclable.setReason(reason);
		recyclable.setOperUser(operationUser);
		//setOperationUser(operationUser);
		recyclable.setOperationTime(operationTime);
		return recyclable;
	}

	@Override
	public IRecyclableObjectRow get(Long recyclableId) {
		return this.query(recyclableId);
	}

	@Override
	public void deleteData(IRecyclableObjectRow row) {
		this.delete(row);		
	}

	@Override
	public IRecyclableObjectRowSet get(String boName, List<Long> operationUserIds, 
			Long startTime, Long endTime, Integer pageNo, Integer itemsPerPage) {	
		
		JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
		Criteria criteria = Criteria.AND();
		if (boName != null) {
			criteria.eq(BO_NAME_FIELD, boName);	
		}
		if (operationUserIds != null && !operationUserIds.isEmpty()) {
			criteria.in(OPERATION_USER_FIELD, operationUserIds.toArray());
		}
		if (startTime != null) {
			criteria.ge(OPERATION_TIME_FIELD, startTime);
		}
		if (endTime != null) {
			criteria.le(OPERATION_TIME_FIELD, endTime);
		}
			
		jsonQueryBuilder.addCriteria(criteria);
		jsonQueryBuilder.addOrderDesc(OPERATION_TIME_FIELD);		

	    pageNo = (pageNo != null && pageNo > 0) ? pageNo : 1;
	    itemsPerPage = (itemsPerPage != null && itemsPerPage > 0) ? itemsPerPage : 20;
        jsonQueryBuilder.setFirstResult((pageNo - 1) * itemsPerPage);
        jsonQueryBuilder.setMaxResult(itemsPerPage);
		
		return this.query(jsonQueryBuilder.toJsonQuerySpec());
	}
	
	@Override
	public Integer getCount(String boName, List<Long> operationUserIds, 
			Long startTime, Long endTime){
		Integer count = 0;
		JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
		Criteria criteria = Criteria.AND();
		if (boName != null) {
			criteria.eq(BO_NAME_FIELD, boName);	
		}
		if (operationUserIds != null && !operationUserIds.isEmpty()) {
			criteria.in(OPERATION_USER_FIELD, operationUserIds.toArray());
		}
		if (startTime != null) {
			criteria.ge(OPERATION_TIME_FIELD, startTime);
		}
		if (endTime != null) {
			criteria.le(OPERATION_TIME_FIELD, endTime);
		}			
		jsonQueryBuilder.addCriteria(criteria);
		count = this.getRowCount(jsonQueryBuilder.toJsonQuerySpec());
		return count;
	}

	@Override
	public void deleteAll() {		
		JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
		Criteria criteria = Criteria.NOT();
		criteria.empty(ID_FIELD);
		jsonQueryBuilder.addCriteria(criteria);
		BoSession boSession = AppContext.session();
		getBusinessObjectHome().batchDelete(boSession, jsonQueryBuilder.toJsonQuerySpec(), true);	
	}

	@Override
	public List<String> delete(List<Long> recyclableObjectIds) {
		JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
		Criteria criteria = Criteria.AND();		
		criteria.in(ID_FIELD, recyclableObjectIds.toArray());						       
		jsonQueryBuilder.addCriteria(criteria);
		String jsonQuerySpec = jsonQueryBuilder.toJsonQuerySpec();		
	
		IRecyclableObjectRowSet rows = this.query(jsonQuerySpec);		
		if (rows == null || rows.getRows().isEmpty()) {
			return null;
		}
		BoSession boSession = AppContext.session();
		getBusinessObjectHome().batchDelete(boSession, recyclableObjectIds, true);
		
		List<String> deletedPks = new ArrayList<String>();
		for (IBusinessObjectRow row : rows.getRows()) {
			String batchPks = ((IRecyclableObjectRow) row).getObjectPk();
			String[] pks = batchPks.split(",");
			deletedPks.addAll(Arrays.asList(pks));
		}		
		
		return deletedPks;
	}

	@Override
	public List<Long> getOperationUsers(String boName) {
		String jsonQuerySpec = "SELECT distinct u.id AS uid FROM " + this.getBusinessObjectId() 
				+ " c LEFT JOIN c.operUser u WHERE c.boName = '" + boName + "'";
		List<Map<String, Object>> userIds = this.runCQLQuery(jsonQuerySpec);
		if (userIds == null || userIds.isEmpty()) {
			return null;
		}
		List<Long> result = new ArrayList<>();
		for (int i = 0; i < userIds.size(); i++) {
            result.add((Long) userIds.get(i).get("uid"));
        }
        return result;
	}	

}
