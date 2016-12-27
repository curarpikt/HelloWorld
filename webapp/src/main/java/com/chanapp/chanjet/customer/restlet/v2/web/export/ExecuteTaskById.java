package com.chanapp.chanjet.customer.restlet.v2.web.export;

import com.chanapp.chanjet.customer.service.exporttask.ExportTaskServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 备份-执行任务
 * 
 * @author tds
 *
 */
public class ExecuteTaskById extends BaseRestlet {
    @Override
    public Object run() {
        Long id = this.getParamAsLong("id");

        return ServiceLocator.getInstance().lookup(ExportTaskServiceItf.class).executeTaskById(id);
    }

}
