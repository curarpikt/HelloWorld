package com.chanapp.chanjet.customer.service.idmove;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.alibaba.fastjson.JSONArray;
import com.chanapp.chanjet.customer.businessobject.api.operationlog.IOperationLogRow;
import com.chanapp.chanjet.customer.constant.metadata.OperationLogMetaData;
import com.chanapp.chanjet.customer.service.operationlog.OperationLogServiceItf;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.context.AppContext;
import com.chanapp.chanjet.web.service.BaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanapp.chanjet.web.util.QueryLimitUtil;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.BoTransactionManager;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.common.base.util.TransactionTracker;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class IdMoveService extends BaseServiceImpl{
	private static final String ID_MOVED = "moved";
	public static final String ID_MOVEDTS = "movedTS";

  private List<Map<String, Object>> runCQLQuery(String cqlQueryString, HashMap<String, Object> paraMap) {
        return QueryLimitUtil.runCQLQuery(null, session(), cqlQueryString, paraMap);
    }

	public void repairMuitDelLogIds() {
		BoSession session = AppContext.session();
		String flag = AppWorkManager.getEnterpriseSettingIdMoveProcessingRequirement(session);
		//String flag = EnterpriseSetting.getValueByApp(session, EnterpriseSetting.IDMOVE_PROCESSING_REQUIRED);
		if (flag == null || flag.equals(ID_MOVED)) {
			// PriorSettingUtil.setApplicationValue(IdMOVEDTS, "");
		} else {
			OperationLogServiceItf service = ServiceLocator.getInstance().lookup(OperationLogServiceItf.class);
			List<IBusinessObjectRow> operationLogList = service.getHistoryMultiDelete().getRows();
			
			Map<Long, Long> movedIdMap = getMovedIdList("com.chanapp.chanjet.customer.entity.Customer");
			Map<Long, String> repairMap = new HashMap<Long, String>();
			for (IBusinessObjectRow row : operationLogList) {
				IOperationLogRow operationLogRow = (IOperationLogRow) row;
				String content = operationLogRow.getContent();
				com.alibaba.fastjson.JSONObject contentObject = com.alibaba.fastjson.JSONObject.parseObject(content);
				JSONArray ids = contentObject.getJSONArray("ids");
				List<Long> repairIds = new ArrayList<Long>();
				Boolean needRepair = false;
				for (Iterator it = ids.iterator(); it.hasNext();) {
					Object id = it.next();
					if (id != null) {
						Long origId = Long.parseLong(id.toString());
						if (origId < 100000) {
							needRepair = true;
							Long toId = movedIdMap.get(origId);
							repairIds.add(toId);
						} else {
							repairIds.add(origId);
						}
					}
				}
				if (needRepair) {
					Map<String, Object> repairContent = new HashMap<String, Object>();
					String reason = contentObject.getString("reason");
					repairContent.put("reason", reason);
					repairContent.put("ids", repairIds);
					repairMap.put(operationLogRow.getId(), com.alibaba.fastjson.JSONObject.toJSONString(repairContent));
					// operationLogRow.setContent(com.alibaba.fastjson.JSONObject.toJSONString(repairIds))
				}
			}
	           TransactionTracker tracker = null;
	           BoTransactionManager tranManger = null; 
			try {
				tranManger = AppWorkManager.getBoTransactionManager();
				tracker = tranManger.beginTransaction();
				repairOperationLog(repairMap);
				Long movedTs = new Date().getTime();
				AppWorkManager.getPriorSetting().setApplicationValue(ID_MOVEDTS, movedTs.toString());
				//PriorSettingUtil.setApplicationValue(ID_MOVEDTS, movedTs.toString());
				AppWorkManager.setEnterpriseSettingIdMoveProcessingRequirement(session, ID_MOVED);
				//EnterpriseSetting.setValueByApp(session, EnterpriseSetting.IDMOVE_PROCESSING_REQUIRED, ID_MOVED);
				tranManger.commitTransaction(session, tracker);
			} catch (Exception e) {
	            if (tracker != null && session != null && session.getTransaction().isActive()) {
	            	tranManger.rollbackTransaction(session);
	            }
			}
		}

	}

	private void repairOperationLog(Map<Long, String> repairMap) {
		int index = 0;
		for (Map.Entry<Long, String> entry : repairMap.entrySet()) {
			index++;
			Long id = entry.getKey();
			String content = entry.getValue();
			JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
			Criteria criteria = Criteria.AND();
			criteria.eq(SC.id, id);
			jsonQueryBuilder.addCriteria(criteria);
			String jsonQuerySpec = jsonQueryBuilder.toJsonQuerySpec();
			OperationLogServiceItf service = ServiceLocator.getInstance().lookup(OperationLogServiceItf.class);
			service.batchUpdate(jsonQuerySpec, new String[] { OperationLogMetaData.content }, new Object[] { content });
			//operationHome.batchUpdate(TransactionManager.getAvailableSession(), jsonQuerySpec, new String[] { OperationLogMetaData.content }, new Object[] { content });
			if (index % 500 == 0) {
				AppWorkManager.setJMXAppProgress(50);
			}
		}
		AppWorkManager.setJMXAppProgress(100);

	}

	public Map<Long, Long> getMovedIdList(String entityId) {
		String hql = "select toId,fromId from com.chanjet.system.systemapp.businessobject.CspIdMove where entityId =:entityId";
		HashMap<String, Object> paraMap = new HashMap<String, Object>();
		paraMap.put("entityId", entityId);
		List<Map<String, Object>> list =runCQLQuery(hql, paraMap);
				
		Map<Long, Long> dataMap = new HashMap<Long, Long>();
		if (list != null) {
			int size = list.size();
			for (int i = 0; i < size; i++) {
				Map<String, Object> obj = (Map<String, Object>) list.get(i);
				Object toId = obj.get("toId");
				Object fromId = obj.get("fromId");	
				if(toId!=null&&fromId!=null){
					dataMap.put(Long.parseLong(fromId.toString()), Long.parseLong(toId.toString()));	
				}							
			}
		}
		return dataMap;
	}
	
	public Set<Long> getMovedIds(String entityName){
		Set<Long> movedIds = new TreeSet<Long>();
		String hql = "select fromId from com.chanjet.system.systemapp.businessobject.CspIdMoveLog where entityId =:entityId";
		HashMap<String, Object> paraMap = new HashMap<String, Object>();
		String entityId = "com.chanapp.chanjet.customer.entity."+entityName;
		paraMap.put("entityId", entityId);
		List<Map<String, Object>> list = runCQLQuery(hql, paraMap);
		if (list != null) {
			int size = list.size();
			for (int i = 0; i < size; i++) {
				Object obj = list.get(i).get("fromId");
				if (obj != null) {						
					movedIds.add(Long.parseLong(obj.toString()));
				}
			}
		}
		return movedIds;	
	}

}
