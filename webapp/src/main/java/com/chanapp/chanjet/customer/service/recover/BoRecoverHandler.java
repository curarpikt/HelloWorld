package com.chanapp.chanjet.customer.service.recover;

import com.chanjet.csp.bo.api.IBusinessObjectRow;

public interface BoRecoverHandler {
	public IBusinessObjectRow insert(String boName, IBusinessObjectRow boRow);
}
