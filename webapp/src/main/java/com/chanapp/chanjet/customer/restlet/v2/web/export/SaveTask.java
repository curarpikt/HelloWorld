package com.chanapp.chanjet.customer.restlet.v2.web.export;

import com.chanapp.chanjet.customer.service.exporttask.ExportTaskServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 备份-创建任务
 * 
 * @author tds
 *
 */
public class SaveTask extends BaseRestlet {
    @Override
    public Object run() {
        return ServiceLocator.getInstance().lookup(ExportTaskServiceItf.class).saveTask();
    }

}