package com.chanapp.chanjet.customer.eventhandler.handler;


import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.common.base.json.JSONObject;

public interface SystemEntityHandlerItf {
	public void handleUpdate(BoSession session, String entityId, JSONObject entity);
/*
	public void handleDelete(BoSession session, String entityId, JSONObject entity);

	public void handleInsert(BoSession session, String entityId, List<Long> ids);*/
}
