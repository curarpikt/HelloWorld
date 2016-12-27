package com.chanapp.chanjet.customer.restlet.v2.web.report;

import com.chanapp.chanjet.customer.service.report.ReportServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 新增客户量分析
 * 
 * @author tds
 *
 */
public class AddCustomersAnalysis extends BaseRestlet {
    @Override
    public Object run() {
        String payload = this.getPayload();
        Assert.notNull(payload);

        return ServiceLocator.getInstance().lookup(ReportServiceItf.class).getAddWeekCustomerAnalysisData(payload);
    }

}
