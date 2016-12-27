package com.chanapp.chanjet.customer.restlet.v2.web.customer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.common.base.exception.AppException;

/**
 * 按条件批量删除客户
 * 
 * @author tds
 *
 */
public class DeleteByConditon extends BaseRestlet {

    @Override
    public Object run() {
        try {
            Map<String, Object> map = dataManager.jsonStringToMap(this.getPayload());

            String condtions = (String) map.get("condtions");
            String reason = (String) map.get("reason");

            CustomerServiceItf customerService = ServiceLocator.getInstance().lookup(CustomerServiceItf.class);

            HashMap<String, Object> result = new HashMap<String, Object>();
            List<Long> ids = customerService.getCustomerIdsByCondtion(condtions);
            customerService.deleteByIds(ids, reason);
            result.put("success", ids.size());
            return result;
        } catch (Exception e) {
            throw new AppException(e, "app.common.server.error");
        }
    }

}
