package com.chanapp.chanjet.customer.restlet.v2.web;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 工作记录新增/删除/查询
 * 
 * @author tds
 *
 */
public class WorkRecord extends BaseRestlet {
    private static final Logger logger = LoggerFactory.getLogger(WorkRecord.class);

    @Override
    public Object run() {
        if (this.getMethod() == MethodEnum.DELETE) {
            return _delete();
        } else if (this.getMethod() == MethodEnum.GET) {
            return _get();
        }

        String payload = this.getPayload();
        Assert.notNull(payload);

        logger.info("add workrecord:{}", payload);

        LinkedHashMap<String, Object> workRecordParam = (LinkedHashMap<String, Object>) dataManager
                .jsonStringToMap(payload);

        Row row = new Row();
        ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).addWorkRecord(workRecordParam, row);
        return row;
    }

    private Object _delete() {
        Long workRecordId = this.getId();
        Assert.notNull(workRecordId);

        ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).deleteWorkRecord(workRecordId);

        Map<String, Object> rs = new HashMap<String, Object>();
        rs.put("success", true);
        return rs;
    }

    private Object _get() {
        Long workRecordId = this.getId();
        Assert.notNull(workRecordId);

        return ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).findWorkRecord(workRecordId);
    }

}
