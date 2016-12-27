package com.chanapp.chanjet.customer.restlet.v2.web;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.service.contact.ContactServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 联系人新增/编辑/删除/查询
 * 
 * @author tds
 *
 */
public class Contact extends BaseRestlet {
    private static final Logger logger = LoggerFactory.getLogger(Contact.class);

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

        logger.info("add contact:{}", payload);

        LinkedHashMap<String, Object> contactParam = (LinkedHashMap<String, Object>) dataManager
                .jsonStringToMap(payload);

        return ServiceLocator.getInstance().lookup(ContactServiceItf.class).addContact(contactParam);

    }

    private Object _put() {
        String payload = this.getPayload();
        Assert.notNull(payload);

        logger.info("edit contact:{}", payload);

        LinkedHashMap<String, Object> contactParam = (LinkedHashMap<String, Object>) dataManager
                .jsonStringToMap(payload);

        return ServiceLocator.getInstance().lookup(ContactServiceItf.class).updateContact(contactParam);
    }

    private Object _delete() {
        Long contactId = this.getId();
        Assert.notNull(contactId);

        ServiceLocator.getInstance().lookup(ContactServiceItf.class).deleteContact(contactId);

        Map<String, Object> rs = new HashMap<String, Object>();
        rs.put("success", true);
        return rs;
    }

    private Object _get() {
        Long contactId = this.getId();
        Assert.notNull(contactId);

        return ServiceLocator.getInstance().lookup(ContactServiceItf.class).getContact(contactId);
    }

}
