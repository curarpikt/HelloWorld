package com.chanapp.chanjet.customer.restlet.v2.web.todowork;

import com.chanapp.chanjet.customer.service.todowork.TodoWorkServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 获取待办提醒类型
 * 
 * @author tds
 *
 */
public class GetTodoRemindType extends BaseRestlet {
    @Override
    public Object run() {
        return ServiceLocator.getInstance().lookup(TodoWorkServiceItf.class).getTodoRemindType();
    }
}
