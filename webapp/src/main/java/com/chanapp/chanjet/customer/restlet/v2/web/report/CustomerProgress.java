package com.chanapp.chanjet.customer.restlet.v2.web.report;

import com.chanapp.chanjet.customer.service.report.ReportServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 客户进度统计统计
 * 
 * @author tds
 *
 */
public class CustomerProgress extends BaseRestlet {
    @Override
    public Object run() {
        String countType = this.getParam("countType");
        Long userId = this.getParamAsLong("userId");
        Long startDate = this.getParamAsLong("startDate");
        Long endDate = this.getParamAsLong("endDate");

        return ServiceLocator.getInstance().lookup(ReportServiceItf.class).customerProgress(countType, userId,
                startDate, endDate);
    }

}