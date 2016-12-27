package com.chanapp.chanjet.web.service;

import com.chanapp.chanjet.web.context.AppContext;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoDataAccessManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.IBusinessObjectHome;
import com.chanjet.csp.bo.api.IBusinessObjectManager;
import com.chanjet.csp.cmr.api.IAppMetadataManager;
import com.chanjet.csp.data.api.DataManager;

/**
 * 所有Service的基础实现类
 * @author tds
 */
public abstract class BaseServiceImpl implements BaseServiceItf {
  protected static final DataManager dataManager = AppWorkManager.getDataManager();
  protected static final BoDataAccessManager boDataAccessManager = AppWorkManager.getBoDataAccessManager();
  protected static final IAppMetadataManager metaDataManager = AppWorkManager.getAppMetadataManager();

  protected BoSession session() {
    return AppContext.session();
  }

  protected IBusinessObjectHome getBusinessObjectHome(String boName) {
    IBusinessObjectManager boManager = AppWorkManager.getBusinessObjectManager();
    return boManager.getPrimaryBusinessObjectHome(boName);
  }

  protected String getBusinessObjectId(String boName) {
    return getBusinessObjectHome(boName).getDefinition().getId();
  }

}
