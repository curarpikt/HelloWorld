package com.chanapp.chanjet.customer.service.recycle;

import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.recyclablerelation.IRecyclableRelationRow;
import com.chanapp.chanjet.customer.businessobject.api.recyclablerelation.IRecyclableRelationRowSet;

public interface RecyclableRelationService {

	public void add(String sourceBo, String sourcePk, String targetBo, String targetPk);

	public IRecyclableRelationRowSet getAllReferencesOf(String pk, boolean source);
	// String --> source or target
	public List<Long> addReferences(Map<String, List<BoReference>> references);

	public void updateStatusOf(String theObjectPk, boolean whenAdd);

	public void deleteInvalidReferences();

	public IRecyclableRelationRowSet getSpecificReferencesOf(String objectPk, 
			List<String> referenceBos, boolean sourceReference);

	public void deleteAll();

	public void deleteReferencesOf(List<String> deletedPks);

	public void updateObjectPks(Map<String, String> pkMap);

	public IRecyclableRelationRow get(Long relationId);

}
