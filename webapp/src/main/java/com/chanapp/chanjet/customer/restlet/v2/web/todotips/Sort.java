package com.chanapp.chanjet.customer.restlet.v2.web.todotips;

import com.chanapp.chanjet.customer.service.todotips.TodoTipsServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 待办事项快捷语-排序
 * 
 * @author tds
 *
 */
public class Sort extends BaseRestlet {
    @Override
    public Object run() {
        return ServiceLocator.getInstance().lookup(TodoTipsServiceItf.class).sortTodoTips(this.getParam("ids"));
    }

}
