package com.chanapp.chanjet.customer.restlet.v2.web.workrecord;

import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 根据客户分页查询工作记录
 * 
 * @author tds
 *
 */
public class ListByCustomer extends BaseRestlet {
    @Override
    public Object run() {
        Long customerId = this.getId();
        Assert.notNull(customerId);

        int pageNo = this.getParamAsInt("pageno");
        int pageSize = this.getParamAsInt("pagesize");
        String status = this.getParam("status");

        return ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).findCustomerWorkRecords(customerId,
                pageNo, pageSize, status);

    }
}
