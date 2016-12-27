package com.chanapp.chanjet.customer.restlet.v2.web;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.service.todowork.TodoWorkServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 待办：新增/编辑/删除/查询
 * 
 * @author tds
 *
 */
public class TodoWork extends BaseRestlet {
    private static final Logger logger = LoggerFactory.getLogger(TodoWork.class);

    @Override
    public Object run() {
        if (this.getMethod() == MethodEnum.PUT) {
            return _put();
        } else if (this.getMethod() == MethodEnum.DELETE) {
            return _delete();
        } else if (this.getMethod() == MethodEnum.GET) {
            return _get();
        }

        String payload = this.getPayload();
        Assert.notNull(payload);

        logger.info("add todowork:{}", payload);

        LinkedHashMap<String, Object> todoWorkParam = (LinkedHashMap<String, Object>) dataManager
                .jsonStringToMap(payload);

        return ServiceLocator.getInstance().lookup(TodoWorkServiceItf.class).addTodoWork(todoWorkParam);

    }

    private Object _put() {
        String payload = this.getPayload();
        Assert.notNull(payload);

        logger.info("edit todowork:{}", payload);

        LinkedHashMap<String, Object> todoWorkParam = (LinkedHashMap<String, Object>) dataManager
                .jsonStringToMap(payload);

        return ServiceLocator.getInstance().lookup(TodoWorkServiceItf.class).updateTodoWork(todoWorkParam);
    }

    private Object _delete() {
        Long todoWorkId = this.getId();
        Assert.notNull(todoWorkId);

        ServiceLocator.getInstance().lookup(TodoWorkServiceItf.class).deleteTodoWork(todoWorkId);

        Map<String, Object> rs = new HashMap<String, Object>();
        rs.put("success", true);
        return rs;
    }

    private Object _get() {
        Long todoWorkId = this.getId();
        Assert.notNull(todoWorkId);

        return ServiceLocator.getInstance().lookup(TodoWorkServiceItf.class).getTodoWork(todoWorkId);
    }

}
