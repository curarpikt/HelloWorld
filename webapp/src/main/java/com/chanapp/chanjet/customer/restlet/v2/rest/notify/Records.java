package com.chanapp.chanjet.customer.restlet.v2.rest.notify;

import com.chanapp.chanjet.customer.service.notify.NotifyServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * @author tds
 *
 */
public class Records extends BaseRestlet {
    @Override
    public Object run() {
        Long timeline = this.getParamAsLong("timeline");
        String categories = this.getParam("categories");
        int count = this.getParamAsInt("count");

        if (count == 0 || count > 100) {
            count = 20;
        }

        return ServiceLocator.getInstance().lookup(NotifyServiceItf.class).records(timeline, categories, count);
    }

}
