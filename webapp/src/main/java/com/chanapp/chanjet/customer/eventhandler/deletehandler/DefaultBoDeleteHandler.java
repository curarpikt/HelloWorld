package com.chanapp.chanjet.customer.eventhandler.deletehandler;

import java.util.List;

import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.cmr.api.metadata.userschema.type.businessObject.IBusinessObject;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.IEntity;
import com.chanjet.csp.common.base.json.JSONObject;

public class DefaultBoDeleteHandler implements BoDeleteHandler {

  @Override
  public void handle(JSONObject boUpdateJson, Long userId, String appId) {
    // DO NOTHING
  }

@Override
public boolean vote(BoSession session, String sourceAppId, IEntity entity, Long rowId,
		List<IBusinessObject> affectedBoList) {
	return true;
	
}

}
