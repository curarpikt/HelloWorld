package com.chanapp.chanjet.customer.restlet.v2.rest.sync;

import com.chanapp.chanjet.customer.service.sync.SyncServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 移动端同步下拉数据接口
 * 
 * @author tds
 *
 */
public class LoadCSV extends BaseRestlet {
    @Override
    public Object run() {
        return ServiceLocator.getInstance().lookup(SyncServiceItf.class).loadCsvData(getPayload());
    }

}
