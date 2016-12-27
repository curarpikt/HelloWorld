package com.chanapp.chanjet.customer.restlet.v2.rest;

import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.service.contact.ContactServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 联系人名片扫描
 * 
 * @author tds
 *
 */
public class Contact extends BaseRestlet {
    private static final Logger logger = LoggerFactory.getLogger(Contact.class);

    @Override
    public Object run() {
        String payload = this.getPayload();
        Assert.notNull(payload);

        logger.info("scan contact:{}", payload);

        LinkedHashMap<String, Object> contactParam = (LinkedHashMap<String, Object>) dataManager
                .jsonStringToMap(payload);

        return ServiceLocator.getInstance().lookup(ContactServiceItf.class).scanCard(contactParam);

    }

}
