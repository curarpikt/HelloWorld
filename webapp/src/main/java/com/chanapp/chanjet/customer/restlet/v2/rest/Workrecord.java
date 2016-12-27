package com.chanapp.chanjet.customer.restlet.v2.rest;

import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 查询工作记录明细
 * 
 * @author tds
 *
 */
public class Workrecord extends BaseRestlet {

    @Override
    public Object run() {
        Long workRecordId = this.getId();
        Assert.notNull(workRecordId);

        return ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).findWorkRecord(workRecordId);
    }

}
