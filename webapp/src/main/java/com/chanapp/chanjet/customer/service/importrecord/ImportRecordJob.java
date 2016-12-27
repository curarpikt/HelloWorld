package com.chanapp.chanjet.customer.service.importrecord;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.importrecordnew.IImportRecordNewRow;
import com.chanapp.chanjet.customer.businessobject.api.metadata.constants.BONames;
import com.chanapp.chanjet.customer.service.importrecordnew.ImportRecordNewServiceItf;
import com.chanapp.chanjet.customer.service.importrecordnew.ImportUtil;
import com.chanapp.chanjet.customer.service.importrecordnew.POIUtils;
import com.chanapp.chanjet.web.context.AppContext;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.BoTransactionManager;
import com.chanjet.csp.common.base.util.TransactionTracker;
import com.chanjet.csp.scheduler.api.jobs.CspJob;

public class ImportRecordJob extends CspJob {

	@Override
	public void executeWithDataMap(Map<String, Object> arg0, Timestamp arg1) {
		ImportRecordNewServiceItf importNewService = ServiceLocator.getInstance().lookup(ImportRecordNewServiceItf.class);
		IImportRecordNewRow row = importNewService.getLastestImportReocrd();
		
		if(row!=null){
			Long rowId = row.getId();
			if(!ImportUtil.importInProgess(rowId)){
				//更新最新任务状态		
			    BoSession session = AppWorkManager.getBoDataAccessManager().getBoSession();
			    AppContext.initSession(session);
			    BoTransactionManager tranxManager = AppWorkManager.getBoTransactionManager(); 
			    TransactionTracker tracker = null;		 
				try {
					ImportUtil.setInProgess(rowId);
					tracker = tranxManager.beginTransaction(session);
					File excelFile = importNewService.downExcel(row.getId());
					Map<String, List<String>>  rsMap = POIUtils.excel(excelFile);
					ImportManger manager = new ImportManger(row.getId(), rsMap);	
					manager.registerImportService(BONames.Customer, new ImportCustomerImpl());
					manager.registerImportService(BONames.Contact, new ImportContactImpl());
					manager.registerImportService(BONames.WorkRecord, new ImportWorkRecordImpl());
					manager.importTask(session);
					tranxManager.commitTransaction(session, tracker);
				} catch (Exception e) {
					if (tracker != null && session != null &&session.getTransaction()!=null&&session. getTransaction().isActive())
						tranxManager.rollbackTransaction(session);
					throw e;
				}
				finally {
					ImportUtil.setDone(rowId);
				}
			}
		}
	}

	@Override
	public void onFailure() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSuccess() {
		// TODO Auto-generated method stub

	}

}
