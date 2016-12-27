package com.chanapp.chanjet.customer.restlet.v2.web.export;

import com.chanapp.chanjet.customer.service.exporttask.ExportTaskServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 备份-获取任务列表
 * 
 * @author tds
 *
 */
public class TaskList extends BaseRestlet {
    @Override
    public Object run() {
        Integer pageNo = this.getParamAsInt("pageno");
        Integer pageSize = this.getParamAsInt("pagesize");

        return ServiceLocator.getInstance().lookup(ExportTaskServiceItf.class).taskList(pageNo, pageSize);
    }

}
