package com.chanapp.chanjet.customer.restlet.v2.rest.invite;

import com.chanapp.chanjet.customer.service.cia.CiaServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 获取邀请列表
 * 
 * @author tds
 *
 */
public class Records extends BaseRestlet {
    @Override
    public Object run() {
        int pageNo = this.getParamAsInt("pageno");
        int pageSize = this.getParamAsInt("pagesize");

        return ServiceLocator.getInstance().lookup(CiaServiceItf.class).records(pageSize, pageNo);

    }

}
