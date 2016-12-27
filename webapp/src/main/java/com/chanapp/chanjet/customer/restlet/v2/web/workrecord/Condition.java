package com.chanapp.chanjet.customer.restlet.v2.web.workrecord;

import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 根据工作记录所有者、工作记录状态、工作记录时间分页查询工作记录
 * 
 * @author tds
 *
 */
public class Condition extends BaseRestlet {
    @Override
    public Object run() {
        String queryType = this.getParam("queryType");
        String queryValue = this.getParam("queryValue");
        int pageNo = this.getParamAsInt("pageno");
        int pageSize = this.getParamAsInt("pagesize");

        return ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).queryWordRecrod(queryValue, queryType,
                pageNo, pageSize);
    }

}
