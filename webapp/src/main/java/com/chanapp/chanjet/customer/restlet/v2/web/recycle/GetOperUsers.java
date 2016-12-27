package com.chanapp.chanjet.customer.restlet.v2.web.recycle;

import com.chanapp.chanjet.customer.service.recycle.RecyclableBinManager;
import com.chanapp.chanjet.customer.service.recycle.RecycleServiceItf;
import com.chanapp.chanjet.customer.vo.AppextResult;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 获取操作人
 * 
 * @author tds
 *
 */
public class GetOperUsers extends BaseRestlet {

    @Override
    public Object run() {
        String entityName = this.getParam("entityName");

        return new AppextResult(RecyclableBinManager.getOperationUsers(entityName));
    }

}
