package com.chanapp.chanjet.customer.restlet.v2.web.export;

import com.chanapp.chanjet.customer.service.exporttask.ExportTaskServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 备份-当日数据统计
 * 
 * @author tds
 *
 */
public class GetExportCount extends BaseRestlet {
    @Override
    public Object run() {
        return ServiceLocator.getInstance().lookup(ExportTaskServiceItf.class).getExportCount();
    }

}
