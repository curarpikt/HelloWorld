package com.chanapp.chanjet.customer.restlet.v2.web.workrecord;

import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 查找我关注的客户工作记录列表
 * 
 * @author tds
 *
 */
public class Follows extends BaseRestlet {
    @Override
    public Object run() {
        int pageNo = this.getParamAsInt("pageno");
        int pageSize = this.getParamAsInt("pagesize");

        return ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).getFollows(pageNo, pageSize);
    }

}
