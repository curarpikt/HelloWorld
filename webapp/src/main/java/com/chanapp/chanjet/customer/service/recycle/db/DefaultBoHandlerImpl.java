package com.chanapp.chanjet.customer.service.recycle.db;

import com.chanapp.chanjet.customer.service.recover.BoRecoverHandler;
import com.chanjet.csp.bo.api.IBusinessObjectHome;
import com.chanjet.csp.bo.api.IBusinessObjectRow;

public class DefaultBoHandlerImpl implements BoRecoverHandler {
	private static BoRecoverHandler instance = new DefaultBoHandlerImpl();
	public static BoRecoverHandler getInstance() {
		return instance;
	}
	
	@Override
	public IBusinessObjectRow insert(String boName, IBusinessObjectRow boRow) {
		IBusinessObjectHome boHome = DbHelper.getBoHome(boName);
		boHome.upsert(boRow);
		return boRow;
	}

}
