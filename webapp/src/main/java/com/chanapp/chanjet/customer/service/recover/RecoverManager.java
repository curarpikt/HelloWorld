package com.chanapp.chanjet.customer.service.recover;

import com.chanapp.chanjet.customer.constant.EO;
import com.chanjet.csp.bo.api.IBusinessObjectRow;

public class RecoverManager {

	public static IBusinessObjectRow recoverEntityRow(String boName, IBusinessObjectRow boRow) {
		BoRecoverHandler handler = getBORecoverHandler(boName);		
		return handler.insert(boName, boRow);
	}
	
	private static BoRecoverHandler getBORecoverHandler(String boName){
		BoRecoverHandler recover = null;
		switch (boName) {
		case EO.Contact:
			recover = new ContactRecoverImpl();
			break;
		case EO.WorkRecord:
			recover = new WorkReocrdRecoverImpl();
			break;
		case EO.Customer:
			recover = new CustomerRecoverImpl();			
			break;	
		default:
			recover = new BoRecoverHandlerImpl();
			break;	
		}
		return recover;
	}
}
