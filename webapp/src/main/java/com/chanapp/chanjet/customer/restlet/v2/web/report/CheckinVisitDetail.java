package com.chanapp.chanjet.customer.restlet.v2.web.report;

import com.chanapp.chanjet.customer.service.report.ReportServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 拜访客户数明细表
 * 
 * @author tds
 *
 */
public class CheckinVisitDetail extends BaseRestlet {
    @Override
    public Object run() {
        Long userId = this.getParamAsLong("userId");
        Long startDate = this.getParamAsLong("startDate");
        Long endDate = this.getParamAsLong("endDate");

        return ServiceLocator.getInstance().lookup(ReportServiceItf.class).checkinVisitDetail(userId, startDate,
                endDate);
    }

}
