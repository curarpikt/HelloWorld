package com.chanapp.chanjet.customer.restlet.v2.web.customer;

import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 根据查询条件查询客户
 * 
 * @author tds
 *
 */
public class Condition extends BaseRestlet {
    @Override
    public Object run() {
        String criteria = this.getParam("queryValue");
        int pageNo = this.getParamAsInt("pageno");
        int pageSize = this.getParamAsInt("pagesize");

        return ServiceLocator.getInstance().lookup(CustomerServiceItf.class).customerList(criteria, pageNo, pageSize);
    }

}
