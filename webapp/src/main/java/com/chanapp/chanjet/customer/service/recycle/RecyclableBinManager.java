package com.chanapp.chanjet.customer.service.recycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.vo.UserQuery;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.customer.vo.VORowSet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.bo.api.IBusinessObjectRow;

public class RecyclableBinManager {
	private static final RecyclableBin recyclableBin = new RecyclableBinImpl();	
	
	public static Map<String, Object> put(String boName, Long objectId) {
		return recyclableBin.put(boName, objectId);
	}
	public static Map<String, Object> put(String boName, Long objectId, IBusinessObjectRow boRow, Long userId) {
		return recyclableBin.put(boName, objectId, boRow, userId);
	}
	public static Map<String, Object> putBatch(String boName, List<Long> objectIds, String reason) {
		return recyclableBin.putBatch(boName, objectIds, reason);
	}

	public static List<Long> recycle(Long recyclableObjectId) {
		return recyclableBin.recycle(recyclableObjectId);
	}

	/**
	 * 获取可回收对象数据，不包括引用关系	
	 */
	public static List<BatchRecyclableObject> get(String boName, List<Long> operationUserIds, 
			Long startTime, Long endTime, Integer pageNo, Integer itemsPerPage) {		 
		 return recyclableBin.get(boName, operationUserIds, startTime, endTime, pageNo, itemsPerPage);
	}
	
	/**
	 * 获取可回收对象数据，包括指定的引用关系	
	 */
	public static List<BatchRecyclableObject> getWithReferences(String boName, List<String> sourceBos, List<String> targetBos, 
			List<Long> operationUserIds, Long startTime, Long endTime, Integer pageNo, Integer itemsPerPage) {
		 return recyclableBin.getWithReferences(boName, sourceBos, targetBos, operationUserIds, startTime, endTime, pageNo, itemsPerPage);
	}

	public static int delete(List<Long> recyclableObjectIds) {
		return recyclableBin.delete(recyclableObjectIds);
	}

	public static int clearAll() {
		return recyclableBin.clearAll();
	}

	public static void init() {
		recyclableBin.init();
	}
	public static BatchRecyclableObject get(Long recyclableId) {		
		return recyclableBin.get(recyclableId);
	}
	
	/**
	 * boName
	 * @return 所有的操作用户
	 */
	public static List<UserValue> getOperationUsers(String boName) {
		List<Long> userIds = recyclableBin.getOperationUsers(boName);
		if (userIds == null || userIds.size() < 1) {
			return new ArrayList<UserValue>();
		}
		
		UserQuery query = new UserQuery();
		query.setUserIds(userIds);
		VORowSet<UserValue> rows = ServiceLocator.getInstance().lookup(UserServiceItf.class).getUsersByParam(query);
		List<UserValue> users = rows.getItems();
		return users;
	}
	
	/**
	 * 获取可回收对象数据，不包括引用关系	
	 */
	public static Integer getCount(String boName, List<Long> operationUserIds, 
			Long startTime, Long endTime) {		 
		 return recyclableBin.getCount(boName, operationUserIds, startTime, endTime);
	}
}
