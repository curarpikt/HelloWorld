package com.chanapp.chanjet.customer.restlet.v2.web.recycle;

import java.util.LinkedHashMap;

import com.chanapp.chanjet.customer.service.recycle.RecyclableBinManager;
import com.chanapp.chanjet.customer.service.recycle.RecycleServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.vo.AppextResult;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanapp.chanjet.web.util.ConvertUtil;

/**
 * 还原
 * 
 * @author tds
 *
 */
public class Restore extends BaseRestlet {

    @Override
    public Object run() {
        String payload = this.getPayload();
        Assert.notNull(payload);

        LinkedHashMap<String, Object> param = (LinkedHashMap<String, Object>) dataManager.jsonStringToMap(payload);

        Long recycleId = ConvertUtil.toLong(param.get("recycleId").toString());
/*        String entityIds = param.get("entityIds").toString();
        String[] entityIdArr = entityIds.split(",");*/
        if(recycleId>0){
        	RecyclableBinManager.recycle(recycleId);
        }
        
        //int num = ServiceLocator.getInstance().lookup(RecycleServiceItf.class).restore(recycleId, entityIds);

        return new AppextResult(1);
    }

}
