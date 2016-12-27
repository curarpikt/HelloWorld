package com.chanapp.chanjet.customer.restlet.v2.web.workrecord;

import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 查询未读工作记录数量
 * 
 * @author tds
 *
 */
public class LatestRecord extends BaseRestlet {
    @Override
    public Object run() {
        Row row = new Row();
        row.put("number", ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).latestRecordForWeb());
        return row;
    }
}
