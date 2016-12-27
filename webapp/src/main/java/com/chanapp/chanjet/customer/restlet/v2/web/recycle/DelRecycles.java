package com.chanapp.chanjet.customer.restlet.v2.web.recycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.chanapp.chanjet.customer.service.recycle.RecyclableBinManager;
import com.chanapp.chanjet.customer.service.recycle.RecycleServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.vo.AppextResult;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 回收站 清除多条
 * 
 * @author tds
 *
 */
public class DelRecycles extends BaseRestlet {

    @Override
    public Object run() {

        String payload = this.getPayload();
        Assert.notNull(payload);

        LinkedHashMap<String, Object> param = (LinkedHashMap<String, Object>) dataManager.jsonStringToMap(payload);
        String batchRecyclableIds = param.get("ids").toString();
        String[] ids = batchRecyclableIds.split(",");
        List<Long> toDeleteIds = new ArrayList<Long>();
        for (String id : ids) {
            toDeleteIds.add(Long.valueOf(id));
        }
        int num = RecyclableBinManager.delete(toDeleteIds);

        return new AppextResult(num);
    }

}