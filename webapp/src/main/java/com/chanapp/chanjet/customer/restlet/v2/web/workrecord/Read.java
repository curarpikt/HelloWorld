package com.chanapp.chanjet.customer.restlet.v2.web.workrecord;

import java.util.HashMap;
import java.util.Map;

import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 将提醒的工作记录置为已读
 * 
 * @author tds
 *
 */
public class Read extends BaseRestlet {
    @Override
    public Object run() {
        ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).read();

        Map<String, Object> rs = new HashMap<String, Object>();
        rs.put("success", true);
        return rs;
    }
}
