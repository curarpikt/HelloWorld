package com.chanapp.chanjet.customer.restlet.v2.web.recycle;

import com.chanapp.chanjet.customer.service.recycle.RecycleServiceItf;
import com.chanapp.chanjet.customer.vo.AppextResult;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 导入删除历史数据
 * 
 * @author tds
 *
 */
public class RecHisReocrds extends BaseRestlet {

    @Override
    public Object run() {
        String tag = this.getParam("tag");

        return new AppextResult(ServiceLocator.getInstance().lookup(RecycleServiceItf.class).recHisReocrds(tag));
    }

}
