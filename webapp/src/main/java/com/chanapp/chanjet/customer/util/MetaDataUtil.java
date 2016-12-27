package com.chanapp.chanjet.customer.util;

import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.cmr.api.IAppMetadataManager;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.IEntity;

public class MetaDataUtil {
	public static IEntity getEntityByEOName(String eoName){
		String appId = AppWorkManager.getCurrentAppId();
		IAppMetadataManager metaDataManager = AppWorkManager.getCmrAppMetadataManagerFactory().getAppMetadataManager(appId);
		IEntity entity = metaDataManager.getEntityByName(eoName);
		return entity;
	}
}
