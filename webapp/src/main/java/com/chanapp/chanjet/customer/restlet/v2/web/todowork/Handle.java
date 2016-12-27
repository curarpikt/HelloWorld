package com.chanapp.chanjet.customer.restlet.v2.web.todowork;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.chanapp.chanjet.customer.service.todowork.TodoWorkServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 处理待办
 * 
 * @author tds
 *
 */
public class Handle extends BaseRestlet {
    @Override
    public Object run() {
        String payload = this.getPayload();
        Assert.notNull(payload);

        LinkedHashMap<String, Object> todoWorkParam = (LinkedHashMap<String, Object>) dataManager
                .jsonStringToMap(payload);

        ServiceLocator.getInstance().lookup(TodoWorkServiceItf.class).handleTodoWork(todoWorkParam);

        Map<String, Object> rs = new HashMap<String, Object>();
        rs.put("success", true);
        return rs;
    }

}
