package com.chanapp.chanjet.customer.restlet.v2.rest.sync;

import com.chanapp.chanjet.customer.service.sync.SyncServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 移动端同步保存数据接口
 * 
 * @author tds
 *
 */
public class SyncSave extends BaseRestlet {
    @Override
    public Object run() {
        return ServiceLocator.getInstance().lookup(SyncServiceItf.class).syncSave(getPayload());
    }

}
