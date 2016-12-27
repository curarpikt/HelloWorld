package com.chanapp.chanjet.customer.restlet.v2.web.report;

import com.chanapp.chanjet.customer.service.report.ReportServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 客户构成分析
 * 
 * @author tds
 *
 */
public class Analysisdata extends BaseRestlet {
    @Override
    public Object run() {
        String payload = this.getPayload();
        Assert.notNull(payload);

        return ServiceLocator.getInstance().lookup(ReportServiceItf.class).getCompositionAnalysisData(payload);
    }

}
