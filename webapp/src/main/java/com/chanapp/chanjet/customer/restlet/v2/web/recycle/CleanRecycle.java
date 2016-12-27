package com.chanapp.chanjet.customer.restlet.v2.web.recycle;

import com.chanapp.chanjet.customer.service.recycle.RecyclableBinManager;
import com.chanapp.chanjet.customer.service.recycle.RecycleServiceItf;
import com.chanapp.chanjet.customer.vo.AppextResult;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 清空
 * 
 * @author tds
 *
 */
public class CleanRecycle extends BaseRestlet {

    @Override
    public Object run() {

        int num = RecyclableBinManager.clearAll();

        return new AppextResult(num);
    }

}