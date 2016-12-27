package com.chanapp.chanjet.customer.service.importrecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.importrecordnew.IImportRecordNewRow;
import com.chanapp.chanjet.customer.businessobject.api.metadata.constants.BONames;
import com.chanapp.chanjet.customer.service.importrecordnew.ImportRecordNewServiceItf;
import com.chanapp.chanjet.customer.service.importrecordnew.ImportUtil;
import com.chanapp.chanjet.web.context.AppContext;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.BoTransactionManager;
import com.chanjet.csp.common.base.util.TransactionTracker;
import com.chanjet.csp.data.api.DataManager;

public class ImportManger {

	private final Map<String, ImportServiceItf> handlers = new LinkedHashMap<String, ImportServiceItf>();
	private final Map<String, List<List<ImportData>>> handerDatas = new HashMap<>();
	private static final DataManager dataManager = AppWorkManager.getDataManager();

	private final Long taskId;

	public ImportManger(Long taskId, Map<String, List<String>> data) {
		this.taskId = taskId;
		List<String> customerList = data.get("Customer_");
		if (customerList != null && customerList.size() > 0) {
			handerDatas.put(BONames.Customer, getImportList(customerList));
		}

		List<String> contactList = data.get("Contact_");
		if (contactList != null && contactList.size() > 0) {
			handerDatas.put(BONames.Contact, getImportList(contactList));
		}

		List<String> workRecordList = data.get("WorkRecord_");
		if (workRecordList != null && workRecordList.size() > 0) {
			handerDatas.put(BONames.WorkRecord, getImportList(workRecordList));
		}
	}
	
	public void registerImportService(String key,ImportServiceItf service){
		handlers.put(key, service);
	}

	private List<List<ImportData>> getImportList(List<String> rows) {
		List<List<ImportData>> importData = new ArrayList<List<ImportData>>();
		for (String jsonData : rows) {
			List<ImportData> beanList = new ArrayList<ImportData>();
			Map<String, Map<String, Object>> dataMap = dataManager.fromJSONString(jsonData, LinkedHashMap.class);
			for (Map<String, Object> map : dataMap.values()) {
				ImportData beanData = new ImportData();
				ImportUtil.transMap2Bean(map, beanData);
				beanList.add(beanData);
			}
			importData.add(beanList);
		}
		return importData;
	}

	public Map<String, Object> importTask(BoSession session) {
		ImportRecordNewServiceItf importNewService = ServiceLocator.getInstance()
				.lookup(ImportRecordNewServiceItf.class);
		IImportRecordNewRow importRow = importNewService.query(this.taskId);
		Long total = 0L;
		Long errorCount =0L;
		if(importRow!=null){
			for (Map.Entry<String, ImportServiceItf> entry : handlers.entrySet()) {
				ImportServiceItf importSerivice = entry.getValue();
				List<List<ImportData>> dataList = handerDatas.get(entry.getKey());
				if(dataList!=null&&dataList.size()>0){
					importSerivice.importTask(dataList, this.taskId,session);					
					total = total+dataList.size();
				}

			}
			importRow = importNewService.query(this.taskId);
			importRow.setTotal(total);
			importRow.setErrorCount(errorCount);
			importRow.setRemoveCount(0L);
			importRow.setStatus(2L);
			importRow.getBusinessObjectHome().upsert(session,importRow);	
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", true);
		return result;
	}
	
	public static void setImportRecordStatus(IImportRecordNewRow importRow,Long status){		
		  BoSession localSession = AppWorkManager.getBoDataAccessManager().createLocalBoSession();
		  BoTransactionManager tranxManager = AppWorkManager.getBoTransactionManager(); 
		  TransactionTracker tracker = null;		 
		try {
			tracker = tranxManager.beginTransaction(localSession);
			importRow.setStatus(status);
			importRow.getBusinessObjectHome().upsert(importRow);
			 tranxManager.commitTransaction(localSession, tracker);
		} catch (Exception e) {
			if (tracker != null && localSession != null &&localSession.getTransaction()!=null&&localSession. getTransaction().isActive())
				tranxManager.rollbackTransaction(localSession);
		}
	}

}
