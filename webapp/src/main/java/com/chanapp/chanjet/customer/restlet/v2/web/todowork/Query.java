package com.chanapp.chanjet.customer.restlet.v2.web.todowork;

import com.chanapp.chanjet.customer.service.todowork.TodoWorkServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 查询待办列表
 * 
 * @author tds
 *
 */
public class Query extends BaseRestlet {
    @Override
    public Object run() {
        String status = this.getParam("status");
        Long startDate = this.getParamAsLong("startDate");
        Long endDate = this.getParamAsLong("endDate");

        return ServiceLocator.getInstance().lookup(TodoWorkServiceItf.class).getAllTodoWorks(startDate, endDate,
                status);
    }
}
