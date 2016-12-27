package com.chanapp.chanjet.customer.restlet.v2.web.customer;

import java.util.HashMap;
import java.util.Map;

import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 取消共享
 * 
 * @author tds
 *
 */
public class UnShare extends BaseRestlet {
    @Override
    public Object run() {
        String payload = this.getPayload();
        Assert.notNull(payload);

        ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                .unShareCustomer(dataManager.jsonStringToMap(payload));

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", true);
        return result;
    }

}