package com.chanapp.chanjet.customer.service.metadata;

import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.cache.CSPEnum;
import com.chanapp.chanjet.customer.cache.CustomerMetaData;
import com.chanapp.chanjet.web.service.BaseServiceItf;

public interface MetaDataServiceItf extends BaseServiceItf{

	CustomerMetaData getMetaData(Long version);

	void saveCustomField(FiledSave field);

	void updateCustomField(FiledSave field);

	List<CSPEnum> findENumList();

	Map<String, Object> setFieldPattern(String entityTable, String fieldName, Map<String, Object> fieldMap);
			

}
