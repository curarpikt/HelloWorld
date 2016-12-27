package com.chanapp.chanjet.customer.restlet.v2.web.customer;

import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.common.base.exception.AppException;

/**
 * 客户进度查询
 * 
 * @author tds
 *
 */
public class Process extends BaseRestlet {
    @Override
    public Object run() {
        Long customerId = this.getId();
        Assert.notNull(customerId);

        ICustomerRow row = ServiceLocator.getInstance().lookup(CustomerServiceItf.class).getCustomerById(customerId);
        if (row == null) {
            throw new AppException("app.customer.object.notexist");
        }
        return ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).getCustomerProgress(customerId);
    }

}
