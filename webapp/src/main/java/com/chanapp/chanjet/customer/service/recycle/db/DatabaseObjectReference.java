package com.chanapp.chanjet.customer.service.recycle.db;

import java.util.List;

import com.chanapp.chanjet.customer.service.recycle.BoReference;

public interface DatabaseObjectReference {
	
	public void delete();
	
	public Long save();

	public String getJsonData();
	
	public List<BoReference> getSourceReferences();
	public List<BoReference> getTargetReferences();

	public String getPk();	

	public Long getObjectId();
	
	public Object getFieldValue(String fieldName);

	public String getName();
	/**
	 * 判断该对象是否被引用
	 * @return
	 */
	public boolean isReferred();

	
}
