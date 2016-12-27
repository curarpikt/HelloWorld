package com.chanapp.chanjet.customer.restlet.v2.web.export;

import com.chanapp.chanjet.customer.service.exporttask.ExportTaskServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 备份-当日任务获取
 * 
 * @author tds
 *
 */
public class GetExportTasksToday extends BaseRestlet {
    @Override
    public Object run() {
        return ServiceLocator.getInstance().lookup(ExportTaskServiceItf.class).getExportTasksToday();
    }

}