package com.chanapp.chanjet.customer.restlet.v2.web.todowork;

import com.chanapp.chanjet.customer.service.todowork.TodoWorkServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 统计完成代办数量
 * 
 * @author tds
 *
 */
public class CountDoneTodoWorks extends BaseRestlet {
    @Override
    public Object run() {
        String timeType = this.getParam("timeType");
        return ServiceLocator.getInstance().lookup(TodoWorkServiceItf.class).countDoneTodoWorks(timeType);
    }

}
