package com.chanapp.chanjet.customer.restlet.v2.rest.customer;

import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 根据客户名称查重
 * 
 * @author tds
 *
 */
public class GetCustomersByName extends BaseRestlet {
    @Override
    public Object run() {
        String name = this.getParam("name");
        String phone = this.getParam("phone");

        return ServiceLocator.getInstance().lookup(CustomerServiceItf.class).getCustomersByNameAndPhone(name, phone);
    }

}