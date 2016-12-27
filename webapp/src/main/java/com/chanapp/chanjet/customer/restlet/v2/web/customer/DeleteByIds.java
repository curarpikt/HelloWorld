package com.chanapp.chanjet.customer.restlet.v2.web.customer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.common.base.exception.AppException;

/**
 * 按主键数组批量删除客户
 * 
 * @author tds
 *
 */
public class DeleteByIds extends BaseRestlet {

    @SuppressWarnings("unchecked")
    @Override
    public Object run() {
        try {
            Map<String, Object> map = dataManager.jsonStringToMap(this.getPayload());

            List<Object> customerIds = (List<Object>) map.get("ids");
            String reason = (String) map.get("reason");

            CustomerServiceItf customerService = ServiceLocator.getInstance().lookup(CustomerServiceItf.class);
            HashMap<String, Object> result = new HashMap<String, Object>();

            List<Long> idList = new ArrayList<Long>();
            for (Object id : customerIds) {
                idList.add(Long.valueOf(id.toString()));
            }
            customerService.deleteByIds(idList, reason);
            result.put("success", idList.size());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(e, "app.common.server.error");
        }
    }

}