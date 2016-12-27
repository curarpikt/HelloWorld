package com.chanapp.chanjet.customer.service.recycle;

import java.sql.Timestamp;
import java.util.List;

import com.chanapp.chanjet.customer.businessobject.api.recyclableobject.IRecyclableObjectRow;
import com.chanapp.chanjet.customer.businessobject.api.recyclableobject.IRecyclableObjectRowSet;

public interface RecyclableObjectService {
	public Long put(String boName, String objectPk, String jsonData, String objectName, 
			String reason, Long operationUser, Timestamp operationTime);
	
	public IRecyclableObjectRow get(Long recyclableObjectId);
	public IRecyclableObjectRowSet get(String boName, List<Long> operationUserIds, Long startTime, Long endTime,
			Integer pageNo, Integer itemsPerPage);
	
	public void deleteData(IRecyclableObjectRow row);

	public void deleteAll();

	/**
	 * 
	 * @param recyclableObjectIds 
	 * @return 删除对象的PK集合
	 */
	public List<String> delete(List<Long> recyclableObjectIds);

	public List<Long> getOperationUsers(String boName);

	Integer getCount(String boName, List<Long> operationUserIds, Long startTime, Long endTime);
}
