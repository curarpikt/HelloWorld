package com.chanapp.chanjet.customer.restlet.v2.web.report;

import com.chanapp.chanjet.customer.service.report.ReportServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 工作记录统计
 * 
 * @author tds
 *
 */
public class WorkrecordProgress extends BaseRestlet {
    @Override
    public Object run() {
        String countType = this.getParam("countType");

        return ServiceLocator.getInstance().lookup(ReportServiceItf.class).workrecordProgress(countType);
    }

}
