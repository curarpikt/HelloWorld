package com.chanapp.chanjet.customer.restlet.v2.web.recycle;

import java.util.Map;

import com.chanapp.chanjet.customer.service.recycle.RecycleServiceItf;
import com.chanapp.chanjet.customer.vo.AppextResult;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 是否需要导入历史数据
 * 
 * @author tds
 *
 */
public class HisRecycleInfo extends BaseRestlet {

    @Override
    public Object run() {
        Map<String, Object> result = ServiceLocator.getInstance().lookup(RecycleServiceItf.class).hisRecycleInfo();

        return new AppextResult(result);
    }

}
