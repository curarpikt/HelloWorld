package com.chanapp.chanjet.customer.restlet.v2.rest.notify;

import com.chanapp.chanjet.customer.service.notify.NotifyServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 未读消息数量
 * 
 * @author tds
 *
 */
public class UnReadCount extends BaseRestlet {
    @Override
    public Object run() {
        String categories = this.getParam("categories");

        return ServiceLocator.getInstance().lookup(NotifyServiceItf.class).unreadCount(categories);
    }

}