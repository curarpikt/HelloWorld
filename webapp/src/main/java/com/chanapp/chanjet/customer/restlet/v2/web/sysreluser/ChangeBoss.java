package com.chanapp.chanjet.customer.restlet.v2.web.sysreluser;

import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.customer.vo.ProcessResult;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

public class ChangeBoss extends BaseRestlet{

	@Override
	public Object run() {
		Long bossId = this.getParamAsLong("bossId");
		ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class).changeBoss(bossId,boDataAccessManager.getBoSession());
		ProcessResult processResult = new ProcessResult(true);
		return processResult;	
	}

}
