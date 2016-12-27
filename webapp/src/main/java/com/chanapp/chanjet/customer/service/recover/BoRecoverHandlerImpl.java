package com.chanapp.chanjet.customer.service.recover;

import com.chanapp.chanjet.customer.service.operationlog.OperationLogServiceItf;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.IBusinessObjectHome;
import com.chanjet.csp.bo.api.IBusinessObjectManager;
import com.chanjet.csp.bo.api.IBusinessObjectRow;

public class BoRecoverHandlerImpl implements BoRecoverHandler {

	@Override
	public IBusinessObjectRow insert(String boName, IBusinessObjectRow boRow) {
		IBusinessObjectManager boManager = AppWorkManager.getBusinessObjectManager();
		IBusinessObjectHome home = boManager.getPrimaryBusinessObjectHome(boName);
		preInsert(boRow);
		home.upsert(boRow);
		postInsert(boRow, boName);
		return boRow;
	}

	public void preInsert(IBusinessObjectRow boRow) {
		// doNothing
	}

	public void postInsert(IBusinessObjectRow boRow, String boName) {
		OperationLogServiceItf operationLogService = ServiceLocator.getInstance().lookup(OperationLogServiceItf.class);
		operationLogService.generate(boRow);
	}
}
