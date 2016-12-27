package com.chanapp.chanjet.customer.restlet.v2.web.report;

import com.chanapp.chanjet.customer.service.report.ReportServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 考勤统计报表
 * 
 * @author tds
 *
 */
public class AttendanceCountCsv extends BaseRestlet {
    @Override
    public Object run() {
        Long groupId = this.getParamAsLong("groupId");
        Long userId = this.getParamAsLong("userId");
        String countDate = this.getParam("countDate");

        return ServiceLocator.getInstance().lookup(ReportServiceItf.class).attendanceCountCsv(groupId, userId,
                countDate);
    }

}
