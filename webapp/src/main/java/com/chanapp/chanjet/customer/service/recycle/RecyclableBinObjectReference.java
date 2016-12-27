package com.chanapp.chanjet.customer.service.recycle;

import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.recyclableobject.IRecyclableObjectRow;

public interface RecyclableBinObjectReference {
	public IRecyclableObjectRow get();

	public Map<String, Object> save();

	public void delete();

	public String getJsonData();

	public String getBoName();

	public String getObjectPk();

	public List<BoReference> getSourceReferences();
	public List<BoReference> getTargetReferences();

}
