package com.chanapp.chanjet.customer.service.recover;

import com.chanapp.chanjet.customer.constant.metadata.ContactMetaData;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.service.recycle.db.DbHelper;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.common.base.exception.AppException;

public class ContactRecoverImpl extends BoRecoverHandlerImpl {

	@Override
	public void preInsert(IBusinessObjectRow boRow) {		
		Long customerId = DbHelper.getIdOf(ContactMetaData.customer, boRow);
		if (customerId == null) {
			// app.sharecustomer.customer.deleted,21004,该客户已经被删除
			throw new AppException("app.sharecustomer.customer.deleted");	
		}		
		
		CustomerServiceItf customerService = ServiceLocator.getInstance().lookup(CustomerServiceItf.class);			
		IBusinessObjectRow customer = customerService.findByIdWithAuth(customerId);
		if (customer == null) {
			// app.sharecustomer.customer.deleted,21004,该客户已经被删除
			throw new AppException("app.sharecustomer.customer.deleted");
		}
		
		super.preInsert(boRow);
	}

}
