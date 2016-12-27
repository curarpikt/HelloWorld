package com.chanapp.chanjet.customer.restlet.v2.web.customer;

import com.chanapp.chanjet.customer.service.customerfollow.CustomerFollowServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 关注/取消关注客户
 * 
 * @author tds
 *
 */
public class Follow extends BaseRestlet {
    @Override
    public Object run() {
        Long customerId = this.getParamAsLong("customerId");
        Boolean isFollow = this.getParamAsBoolean("isFollow");

        Assert.notNull(customerId);
        Assert.notNull(isFollow);

        ServiceLocator.getInstance().lookup(CustomerFollowServiceItf.class).follow(customerId, isFollow);

        return null;
    }

}
