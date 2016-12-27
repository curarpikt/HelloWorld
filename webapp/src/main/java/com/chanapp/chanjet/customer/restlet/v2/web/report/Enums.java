package com.chanapp.chanjet.customer.restlet.v2.web.report;

import com.chanapp.chanjet.customer.service.report.ReportServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 获取客户枚举字段
 * 
 * @author tds
 *
 */
public class Enums extends BaseRestlet {
    @Override
    public Object run() {
        return ServiceLocator.getInstance().lookup(ReportServiceItf.class).getAllCustomerEnums();
    }

}
