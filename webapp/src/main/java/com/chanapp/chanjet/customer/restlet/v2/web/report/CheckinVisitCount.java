package com.chanapp.chanjet.customer.restlet.v2.web.report;

import com.chanapp.chanjet.customer.service.report.ReportServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 拜访客户统计报表
 * 
 * @author tds
 *
 */
public class CheckinVisitCount extends BaseRestlet {
    @Override
    public Object run() {
        Long groupId = this.getParamAsLong("groupId");
        Long userId = this.getParamAsLong("userId");
        String countDate = this.getParam("countDate");
        String countType = this.getParam("countType");

        return ServiceLocator.getInstance().lookup(ReportServiceItf.class).checkinVisitCount(groupId, userId, countDate,
                countType);
    }

}
