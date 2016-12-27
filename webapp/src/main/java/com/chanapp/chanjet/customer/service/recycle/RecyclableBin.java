package com.chanapp.chanjet.customer.service.recycle;

import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.recyclableobject.IRecyclableObjectRow;
import com.chanjet.csp.bo.api.IBusinessObjectRow;

public interface RecyclableBin {
	// add object to recyclable bin
	public Map<String, Object> put(String boName, Long objectId);
	public Map<String, Object> put(String boName, Long objectId, IBusinessObjectRow boRow, Long userId);
	public Map<String, Object> putBatch(String boName, List<Long> objectIds, String reason);
	
	public List<Long> recycle(Long recyclableObjectId);
	
	public List<BatchRecyclableObject> get(String boName, List<Long> operationUserIds, 
			Long startTime, Long endTime, Integer pageNo, Integer itemsPerPage);
	
	public List<BatchRecyclableObject> getWithReferences(String boName, List<String> sourceBos, List<String> targetBos,
			List<Long> operationUserIds, Long startTime, Long endTime, Integer pageNo, Integer itemsPerPage);
	public BatchRecyclableObject get(Long recyclableId);
	
	public int delete(List<Long> recyclableObjectIds);
	
	public int clearAll();
	
	public void init();
	
	public List<Long> getOperationUsers(String boName);
	Map<String, Object> put(String boName, Long objectId, Long operaterUserId);
	Integer getCount(String boName, List<Long> operationUserIds, Long startTime, Long endTime);
	
}
