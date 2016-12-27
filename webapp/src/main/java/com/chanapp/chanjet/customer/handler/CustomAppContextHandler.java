package com.chanapp.chanjet.customer.handler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.constant.BO;
import com.chanapp.chanjet.customer.constant.EO;
import com.chanapp.chanjet.customer.eventhandler.handler.SysHandlerRegister;
import com.chanapp.chanjet.customer.layout.LayoutManager;
import com.chanapp.chanjet.customer.service.recycle.RecyclableBin;
import com.chanapp.chanjet.customer.service.recycle.RecyclableBinImpl;
import com.chanapp.chanjet.customer.service.role.RoleServiceItf;
import com.chanapp.chanjet.web.context.AppContext;
import com.chanapp.chanjet.web.service.BoCloneRowRegister;
import com.chanapp.chanjet.customer.service.usersetting.UserSettingServiceItf;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.BoTransactionManager;
import com.chanjet.csp.common.base.rest.RestAppContext;
import com.chanjet.csp.common.base.util.TransactionTracker;
import com.chanjet.csp.enterprise.ext.AppContextHandler;

public class CustomAppContextHandler implements AppContextHandler {
    private final static Logger logger = LoggerFactory.getLogger(CustomAppContextHandler.class);

    public void shutdown(RestAppContext context) {

    }

    public boolean startup(RestAppContext context) {
    	   logger.info("CustomAppContextHandler startup!");
           //System.out.println("CustomAppContextHandler startup");

           BoSession session = AppContext.session();
           BoTransactionManager tranxManager = AppWorkManager.getBoTransactionManager();
           TransactionTracker tracker = null;
        try {
        	tracker = tranxManager.beginTransaction(session);
  
        	LayoutManager.initLayout();
        	ServiceLocator.getInstance().lookup(RoleServiceItf.class).initUserRole(session);    
            tranxManager.commitTransaction(session, tracker);
            BoCloneRowRegister.register(BO.Customer,BO.Contact,BO.TodoTips,BO.TodoWork,BO.WorkRecord);
            SysHandlerRegister.register(EO.AppUser);

        } catch (Exception e) {
        	e.printStackTrace();
            if (tracker != null && session != null && session.getTransaction().isActive()) {
                tranxManager.rollbackTransaction(session);
            }
            logger.error(e.getMessage());
            return false;
        }         
        AppWorkManager.setJMXAppProgress(100);
        
/*        try{
    		IdMoveService service =new IdMoveService();
    		service.repairMuitDelLogIds();
        }catch (Exception e) {
        	e.printStackTrace();
            return true;
        }*/
        
        return true;
    }

}
