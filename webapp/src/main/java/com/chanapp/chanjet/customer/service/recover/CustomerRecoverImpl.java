package com.chanapp.chanjet.customer.service.recover;

import com.chanapp.chanjet.customer.constant.OP;
import com.chanapp.chanjet.customer.service.operationlog.OperationLogServiceItf;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.bo.api.IBusinessObjectRow;

public class CustomerRecoverImpl extends BoRecoverHandlerImpl{
	@Override
	public void postInsert(IBusinessObjectRow boRow, String boName) {
		OperationLogServiceItf operationLogService = ServiceLocator.getInstance().lookup(OperationLogServiceItf.class);
		operationLogService.generate(boRow,OP.GRANT);
	}

}
