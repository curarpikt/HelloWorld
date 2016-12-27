package com.chanapp.chanjet.customer.eventhandler.deletehandler;

import java.util.List;

import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.cmr.api.metadata.userschema.type.businessObject.IBusinessObject;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.IEntity;
import com.chanjet.csp.common.base.json.JSONObject;

public interface BoDeleteHandler {
  void handle(JSONObject boUpdateJson, Long userId, String appId);
  boolean vote(BoSession session, String sourceAppId, IEntity entity, Long rowId,
	      List<IBusinessObject> affectedBoList);
}
