package com.chanapp.chanjet.customer.restlet.v2.web.invite;

import com.chanapp.chanjet.customer.service.cia.CiaServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 邀请人员加入企业
 * 
 * @author tds
 *
 */
public class Send extends BaseRestlet {
    @Override
    public Object run() {
        return ServiceLocator.getInstance().lookup(CiaServiceItf.class).send(getPayload(),boDataAccessManager.getBoSession());
    }

}
