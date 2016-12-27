package com.chanapp.chanjet.customer.restlet.v2.web.sysreluser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.customer.vo.ProcessResult;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.web.annotation.NoTransaction;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.BoTransactionManager;
import com.chanjet.csp.common.base.util.TransactionTracker;
import com.chanjet.csp.util.SyncUserUtils;

@NoTransaction
public class SaveHierarchyUsers extends BaseRestlet {

    @SuppressWarnings("unchecked")
    @Override

    public Object run() {
        List<UserValue> users = new ArrayList<UserValue>();
        List<Map<String, Object>> _users = dataManager.fromJSONString(this.getPayload(), List.class);
        for (Map<String, Object> _user : _users) {
            if (_user.containsKey("userLongId")) {
                _user.remove("userLongId");
            }          
            if (_user.containsKey("deleteable")) {
                _user.remove("deleteable");
            } 
            if (_user.containsKey("type")) {
                _user.remove("type");
            }
            if (_user.containsKey("origUserLevel")) {
                _user.remove("origUserLevel");
            }
            if (_user.containsKey("fullSpellOrigin")) {
                _user.remove("fullSpellOrigin");
            }
            if (_user.containsKey("shortSpellOrigin")) {
                _user.remove("shortSpellOrigin");
            } 
           UserValue user = dataManager.fromJSONString(dataManager.toJSONString(_user), UserValue.class);
            
            users.add(user);
        }
        BoSession session = AppWorkManager.getBoDataAccessManager().getBoSession();
           BoTransactionManager tranxManager = AppWorkManager.getBoTransactionManager();
        TransactionTracker tracker = null;
        ProcessResult processResult = null;
		try{
			//ServiceLocator.getInstance().lookup(UserSettingServiceItf.class).getBusinessObjectHome().query(session, userId);
			tracker = tranxManager.beginTransaction(session);	
			List<Long> syncUsers = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class).saveHierarchyUsers(users,session);			   
		    tranxManager.commitTransaction(session, tracker);
		    processResult = new ProcessResult(true);
	        for(Long userId:syncUsers){
	        	SyncUserUtils.syncUserFromCIA(userId, false);
	        }	
	   
		}catch(Exception e){
			//TODO 异常处理
			e.printStackTrace();
            if (tracker != null && session != null && session.getTransaction()!=null&&session.getTransaction().isActive()) {
                tranxManager.rollbackTransaction(session);
            }
		}
	     return processResult;

    }
    


}
