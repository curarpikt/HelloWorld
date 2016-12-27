package com.chanapp.chanjet.customer.restlet.v2.rest.todotips;

import com.chanapp.chanjet.customer.service.todotips.TodoTipsServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 待办事项快捷语-更新
 * 
 * @author tds
 *
 */
public class Update extends BaseRestlet {
    @Override
    public Object run() {
        return ServiceLocator.getInstance().lookup(TodoTipsServiceItf.class).updateTodoTips(this.getParamAsLong("id"),
                this.getParam("todoTips"));
    }

}
