package com.chanapp.chanjet.customer.eventhandler.handler;

import com.chanapp.chanjet.customer.service.user.UserHierarchyManager;
import com.chanapp.chanjet.customer.service.usersetting.UserSettingServiceItf;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.BoTransactionManager;
import com.chanjet.csp.common.base.json.JSONObject;
import com.chanjet.csp.common.base.util.TransactionTracker;

public class AppUserHandler implements SystemEntityHandlerItf {

	@Override
	public void handleUpdate(BoSession session, String entityId, JSONObject entity) {
		String appId = entity.getString("appId");		
		//只处理当前应用的数据
		if (AppWorkManager.getCurrentAppId().equals(appId)) {
			Long userId = null;
			JSONObject user = entity.getJSONObject("user");
			if (user != null) {
				userId = user.getLong("id");
/*				BoSession localSession = AppWorkManager.getBoDataAccessManager().createLocalBoSession();
		        ServiceLocator.getInstance().lookup(UserSettingServiceItf.class).lockAppUserChange(localSession);*/
				boolean origFlag = entity.getBoolean("isAppSuperUser");
				UserHierarchyManager userMa =new UserHierarchyManager(userId,session);
		        BoTransactionManager tranxManager = AppWorkManager.getBoTransactionManager();
		        TransactionTracker tracker = null;
				try{
					//ServiceLocator.getInstance().lookup(UserSettingServiceItf.class).getBusinessObjectHome().query(session, userId);
					tracker = tranxManager.beginTransaction(session);	
					userMa.handerAppUserChange(origFlag);
				    tranxManager.commitTransaction(session, tracker);
				}catch(Exception e){
					//TODO 异常处理
					e.printStackTrace();
		            if (tracker != null && session != null && session.getTransaction()!=null&&session.getTransaction().isActive()) {
		                tranxManager.rollbackTransaction(session);
		            }
				}
			}

		
		}
	}

}
