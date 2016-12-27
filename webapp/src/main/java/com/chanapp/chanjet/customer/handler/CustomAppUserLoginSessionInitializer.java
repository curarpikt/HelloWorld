package com.chanapp.chanjet.customer.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.web.context.AppContext;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.BoTransactionManager;
import com.chanjet.csp.common.base.rest.RestRequest;
import com.chanjet.csp.common.base.rest.RestResponse;
import com.chanjet.csp.common.base.util.TransactionTracker;
import com.chanjet.csp.enterprise.ext.AppUserLoginSessionInitializer;

public class CustomAppUserLoginSessionInitializer implements AppUserLoginSessionInitializer {
    private final static Logger logger = LoggerFactory.getLogger(CustomAppUserLoginSessionInitializer.class);

    public void initUserSession(Long userId, RestRequest req, RestResponse res) {

        BoSession session = AppContext.session();
        BoTransactionManager tranxManager = AppWorkManager.getBoTransactionManager();
        TransactionTracker tracker = null;
     try {
     	 tracker = tranxManager.beginTransaction(session);
        	//先初始化BOSS
     	 UserServiceItf userService =  ServiceLocator.getInstance().lookup(UserServiceItf.class);
     	 userService.initBossSettingValue();
     	 userService.initUser(session);

         tranxManager.commitTransaction(session, tracker);
     } catch (Exception e) {
     	e.printStackTrace();
         if (tracker != null && session != null && session.getTransaction().isActive()) {
             tranxManager.rollbackTransaction(session);
         }
         logger.error(e.getMessage());

     }
        logger.info("initUserSession userId = {} login!", userId);
    }

}
