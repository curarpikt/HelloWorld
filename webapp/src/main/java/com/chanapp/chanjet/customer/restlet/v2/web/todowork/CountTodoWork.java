package com.chanapp.chanjet.customer.restlet.v2.web.todowork;

import com.chanapp.chanjet.customer.service.todowork.TodoWorkServiceItf;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 待办数量统计
 * 
 * @author tds
 *
 */
public class CountTodoWork extends BaseRestlet {
    @Override
    public Object run() {

        String status = this.getParam("status");
        Long startDate = this.getParamAsLong("startDate");
        Long endDate = this.getParamAsLong("endDate");

        TodoWorkServiceItf todoWorkService = ServiceLocator.getInstance().lookup(TodoWorkServiceItf.class);

        todoWorkService.checkParams(status);
        Row row = new Row();
        Long num = todoWorkService.countTodoWorks(startDate, endDate, status);
        row.put("num", num);
        return row;
    }

}
