package com.chanapp.chanjet.customer.restlet.v2.web.todotips;

import com.chanapp.chanjet.customer.service.todotips.TodoTipsServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 待办事项快捷语-列表获取
 * 
 * @author tds
 *
 */
public class Query extends BaseRestlet {
    @Override
    public Object run() {
        return ServiceLocator.getInstance().lookup(TodoTipsServiceItf.class).findTodoTips();
    }

}
