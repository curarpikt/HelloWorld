package com.chanapp.chanjet.customer.eventhandler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.eventhandler.manager.BoDeleteHandlerManager;
import com.chanapp.chanjet.web.context.AppContext;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.BoTransactionManager;
import com.chanjet.csp.cmr.api.metadata.userschema.type.businessObject.IBusinessObject;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.IEntity;
import com.chanjet.csp.common.base.json.JSONObject;
import com.chanjet.csp.common.base.util.TransactionTracker;
import com.chanjet.csp.event.SystemEventHandler;


public class CustomSystemEventHandler extends SystemEventHandler {
  private static final Logger logger = LoggerFactory.getLogger(CustomSystemEventHandler.class);

  /**
   * 删除前触发
   */
  @Override
  protected boolean checkCanDelete(BoSession session, String sourceAppId, IEntity entity, Long rowId,
      List<IBusinessObject> affectedBoList) {
	String boName = entity.getName();
    logger.info("checkCanDelete: boName=[{}]", boName);
	boolean customCheck = BoDeleteHandlerManager.getInstance().getHandler(boName).vote(session, sourceAppId, entity, rowId, affectedBoList);	  
    if(customCheck==false)
    	return false;
	return super.checkCanDelete(session, sourceAppId, entity, rowId, affectedBoList);
  }

  /**
   * 删除后触发
   */
  @Override
  protected void handleDelete(BoSession session, List<IBusinessObject> bos, JSONObject boUpdateJson, Long userId,
      String appId) {

    // 客户管家删除时，不处理
    if (appId.equals(AppWorkManager.getCurrentAppId())) {
      logger.info("handleDelete:appId=[{}]", appId);
      return;
    }

    BoTransactionManager tranxManager = AppWorkManager.getBoTransactionManager();
    TransactionTracker tracker = null;

    try {
      tracker = tranxManager.beginTransaction(session);
      Long boRowId = boUpdateJson.optLong("id");
      IBusinessObject bo = bos.get(0);
      String boName = bo.getName();
      logger.info("handleDelete:boName=[{}],boRowId=[{}],userId=[{}],appId=[{}],boUpdateJson=[{}]", boName, boRowId, userId, appId,boUpdateJson);

      AppContext.initSession(session);

      // 回收
/*      IBusinessObjectHome boHome = AppWorkManager.getBusinessObjectManager().getPrimaryBusinessObjectHome(boName);
    
      @SuppressWarnings("unchecked")
      LinkedHashMap<String, Object> map =
          AppWorkManager.getDataManager().fromJSONString(boUpdateJson.toString(), LinkedHashMap.class);
      LinkedHashMap<String, Object> rowMap = new LinkedHashMap<String, Object>();
      for(Map.Entry<String, Object> entry :map.entrySet()){
    	  String key = entry.getKey();
    	  if(!key.endsWith("ShadowFK")){
    		  rowMap.put(entry.getKey(), entry.getValue());
    	  }
      }*/
    //  IBusinessObjectRow boRow = constructBORow(session,bo,boUpdateJson);
/*      for (SystemFieldNameEnum systemField : SystemFieldNameEnum.values()) {
        boRow.setFieldValue(session, systemField.value(), map.get(systemField.value()));
      }*/
     // RecyclableBinManager.put(boName, boRowId, boRow, userId);

      // 其它自定义处理
      BoDeleteHandlerManager.getInstance().getHandler(boName).handle(boUpdateJson, userId, appId);

      tranxManager.commitTransaction(session, tracker);
    } catch (Exception e) {
      logger.error("handleDelete Exception ", e);
      if (tracker != null && session != null) {
        tranxManager.rollbackTransaction(session);
      }
      throw e;
    }

  }

  /**
   * 新增后触发
   */
  @Override
  protected void handleInsert(BoSession session, List<IBusinessObject> bos, Long rowId, Long userId, String appId) {
    super.handleInsert(session, bos, rowId, userId, appId);
  }

  /**
   * 修改后触发
   */
  @Override
  protected void handleUpdate(BoSession session, List<IBusinessObject> bos, JSONObject boUpdateJson, Long userId,
      String appId) {
    super.handleUpdate(session, bos, boUpdateJson, userId, appId);
  }
}
