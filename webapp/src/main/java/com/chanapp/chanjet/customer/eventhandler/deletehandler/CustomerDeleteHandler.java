package com.chanapp.chanjet.customer.eventhandler.deletehandler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerHome;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.service.checkin.CheckinServiceItf;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.cmr.api.metadata.userschema.type.businessObject.IBusinessObject;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.IEntity;
import com.chanjet.csp.common.base.json.JSONObject;

public class CustomerDeleteHandler extends DefaultBoDeleteHandler {
	private static final Logger logger = LoggerFactory.getLogger(CustomerDeleteHandler.class);

	@Override
	public void handle(JSONObject boUpdateJson, Long userId, String appId) {

	}

	@Override
	public boolean vote(BoSession session, String sourceAppId, IEntity entity, Long rowId,
			List<IBusinessObject> affectedBoList) {
		if (sourceAppId.equals(AppWorkManager.getCurrentAppId())) {
			logger.info("checkCanDelete: sourceAppId=[{}]", sourceAppId);
			return true;
		}
		String boName = entity.getName();
		ICustomerHome bo = (ICustomerHome)AppWorkManager.getBusinessObjectManager().getPrimaryBusinessObjectHome(boName);
		ICustomerRow row = bo.query(session, rowId);
		if(row==null)
			return true;
		if ("com.chanapp.chanjet.customer.entity.Customer".equals(row.getCreatedByEntityId())) {
			logger.info("checkCanDelete: appId=[{}]", row.getCreatedByEntityId());
			return false;
		}
		boolean hasCheckin = ServiceLocator.getInstance().lookup(CheckinServiceItf.class)
				.checkCheckinByCustomerId(rowId);
		if (hasCheckin) {
			logger.info("checkCanDelete: hasCheckin=[{}]", hasCheckin);
			return false;
		}
		return true;
	}

}
