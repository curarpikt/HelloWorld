package com.chanapp.chanjet.customer.service.recover;

import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.businessobject.api.workrecord.IWorkRecordRow;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.bo.api.IBusinessObjectRow;

public class WorkReocrdRecoverImpl extends BoRecoverHandlerImpl {
	@Override
	public void postInsert(IBusinessObjectRow boRow, String boName) {
		CustomerServiceItf customerService = ServiceLocator.getInstance().lookup(CustomerServiceItf.class);
		IWorkRecordRow workRecordRow = (IWorkRecordRow) boRow;
		ICustomerRow customerRow = customerService.findByIdWithAuth(workRecordRow.getCustomer());
		customerService.updateCustomerStatus(customerRow, workRecordRow, false);
		super.postInsert(boRow, boName);
	}
}
